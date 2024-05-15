package at.ac.uibk.dps.cirrina.runtime;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_UPTIME;

import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClassBuilder;
import at.ac.uibk.dps.cirrina.execution.object.context.Context;
import at.ac.uibk.dps.cirrina.execution.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.runtime.job.Job;
import at.ac.uibk.dps.cirrina.runtime.job.JobListener;
import at.ac.uibk.dps.cirrina.runtime.job.JobMonitor;
import com.google.common.collect.ArrayListMultimap;
import io.opentelemetry.api.OpenTelemetry;
import java.io.IOException;
import org.apache.curator.framework.CuratorFramework;

/**
 * Online runtime, a runtime system implementation that is meant to be connected to a message queue, key-value store and coordination system
 * to allow for a distributed deployment of one or several runtime systems.
 * <p>
 * StateClass machine instantiation is triggered based on jobs.
 */
public class OnlineRuntime extends Runtime implements JobListener {

  /**
   * Start time.
   */
  private final long startTime = System.currentTimeMillis();

  /**
   * Job monitor, using ZooKeeper to monitor new jobs.
   */
  private final JobMonitor jobMonitor;

  /**
   * Initializes this online runtime instance.
   *
   * @param eventHandler      Event handler.
   * @param persistentContext Persistent context.
   * @param openTelemetry     OpenTelemetry.
   * @param curatorFramework  CuratorFramework.
   */
  public OnlineRuntime(
      EventHandler eventHandler,
      Context persistentContext,
      OpenTelemetry openTelemetry,
      CuratorFramework curatorFramework
  ) {
    super(eventHandler, persistentContext, openTelemetry);

    // Create a job monitor
    this.jobMonitor = new JobMonitor(curatorFramework, this);
  }

  /**
   * Called upon the arrival of a new job, will instantiate a new state machine if capable and appropriate.
   *
   * @param job New job.
   * @throws UnsupportedOperationException If a state machine is not known.
   */
  @Override
  public void newJob(Job job) throws UnsupportedOperationException {
    // TODO: Check if job can be executed, would be nice to add some conditions

    try {
      // Attempt to lock the job
      job.lock();

      // It is possible that it has been removed in the meantime
      if (job.exists()) {
        // Acquire the job description
        final var jobDescription = job.getJobDescription();

        // Create the collaborative state machine from the description
        final var collaborativeStateMachine = CollaborativeStateMachineClassBuilder.from(job.getJobDescription().collaborativeStateMachine)
            .build();

        // Acquire the service implementation selector
        // TODO: Build the correct one based on the description
        final var serviceImplementationSelector = new ServiceImplementationSelector(ArrayListMultimap.create());

        // Acquire the state machine name
        final var stateMachineName = jobDescription.stateMachineName;

        // Find the state machine by name
        final var stateMachine = collaborativeStateMachine.findStateMachineClassByName(stateMachineName)
            .orElseThrow(() -> new UnsupportedOperationException(
                "A state machine with the name '%s' does not exist in the collaborative state machine".formatted(stateMachineName)));

        // Create persistent variables
        final var persistentContextVariables = collaborativeStateMachine.getPersistentContextVariables();

        persistentContextVariables.forEach(variable -> {
          try {
            persistentContext.create(variable.name(), variable.value());
          } catch (IOException ignored) {
            // Variable likely already exists, ignore. We probably ignore too many errors here, should there be a legitimate error
          }
        });

        // Create a new instance
        newInstance(stateMachine, serviceImplementationSelector, null);

        // Delete the job (it has been consumed)
        job.delete();
      }
    } finally {
      // Unlock the job
      job.unlock();
    }
  }

  /**
   * Run until shut down.
   */
  public void run() {
    // Increment executed actions counter
    try (final var uptime = meter.gaugeBuilder(METRIC_UPTIME)
        .buildWithCallback(measurement -> measurement.record(((double) System.currentTimeMillis() - startTime) / 1000.0))) {
      final var SLEEP_TIME_IN_MS = 10000;

      try {
        while (!isShutdown()) {
          Thread.sleep(SLEEP_TIME_IN_MS);
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
