package at.ac.uibk.dps.cirrina.runtime;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_UPTIME;

import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClassBuilder;
import at.ac.uibk.dps.cirrina.csml.description.ExpressionDescription;
import at.ac.uibk.dps.cirrina.execution.object.context.Context;
import at.ac.uibk.dps.cirrina.execution.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.execution.object.expression.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationBuilder;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.runtime.job.Job;
import at.ac.uibk.dps.cirrina.runtime.job.JobListener;
import at.ac.uibk.dps.cirrina.runtime.job.JobMonitor;
import io.opentelemetry.api.OpenTelemetry;
import java.io.IOException;
import java.util.List;
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
  private final double startTime = System.nanoTime() / 1.0e6;

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
   * @throws UnsupportedOperationException If the state machine is not known or abstract.
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
        final var serviceImplementationSelector = new ServiceImplementationSelector(
            ServiceImplementationBuilder.from(jobDescription.serviceImplementations).build());

        // Acquire the state machine name
        final var stateMachineName = jobDescription.stateMachineName;

        // Find the state machine by name
        final var stateMachine = collaborativeStateMachine.findStateMachineClassByName(stateMachineName)
            .orElseThrow(() -> new UnsupportedOperationException(
                "A state machine with the name '%s' does not exist in the collaborative state machine".formatted(stateMachineName)));

        // Throw an error if the state machine is abstract (should not be instantiated)
        if (stateMachine.isAbstract()) {
          throw new UnsupportedOperationException(
              "State machine '%s' is abstract and can not be instantiated".formatted(stateMachineName));
        }

        // Create persistent variables
        final var persistentContextVariables = collaborativeStateMachine.getPersistentContextVariables();

        persistentContextVariables.forEach(variable -> {
          try {
            persistentContext.create(variable.name(), variable.value());
          } catch (IOException ignored) {
            // Variable likely already exists, ignore. We probably ignore too many errors here, should there be a legitimate error
          }
        });

        // Create instances, newInstances will also instantiate nested state machines
        final var instanceIds = newInstances(List.of(stateMachine), serviceImplementationSelector, null);

        // Assign local data from the job description if the job description contains any local data
        if (!job.getJobDescription().localData.isEmpty()) {

          // Get the created state machine instance
          final var parentInstanceId = instanceIds.stream().findFirst().orElseThrow(() -> new UnsupportedOperationException(
              "State machine '%s' was not instantiated.".formatted(stateMachine.getName())));
          final var stateMachineInstance = findInstance(parentInstanceId).orElseThrow(() -> new UnsupportedOperationException(
              "State machine '%s' with id '%s' was not instantiated.".formatted(stateMachine.getName(), parentInstanceId)));

          for (final var localData : job.getJobDescription().localData.entrySet()) {

            try {
              // Assign local data entry, evaluate the value as an expression
              final var valueExpression = ExpressionBuilder.from(new ExpressionDescription(localData.getValue())).build();

              stateMachineInstance.getExtent().trySet(localData.getKey(), valueExpression.execute(stateMachineInstance.getExtent()));
            } catch (IOException | IllegalArgumentException e) {
              throw new UnsupportedOperationException(
                  "Could not assign value '%s' to local data variable '%s'".formatted(localData.getKey(), localData.getValue()), e);
            }
          }
        }

        // Delete the job (it has been consumed)
        //job.delete();
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
        .buildWithCallback(measurement -> measurement.record((System.nanoTime() / 1.0e6 - startTime)))) {
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
