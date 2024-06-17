package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClassBuilder;
import at.ac.uibk.dps.cirrina.csml.description.ExpressionDescription;
import at.ac.uibk.dps.cirrina.execution.object.context.Context;
import at.ac.uibk.dps.cirrina.execution.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.execution.object.expression.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.execution.service.RandomServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationBuilder;
import at.ac.uibk.dps.cirrina.runtime.job.Job;
import at.ac.uibk.dps.cirrina.runtime.job.JobListener;
import at.ac.uibk.dps.cirrina.runtime.job.JobMonitor;
import at.ac.uibk.dps.cirrina.utils.Time;
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
  private final double startTime = Time.timeInMillisecondsSinceStart();

  /**
   * Job monitor, using ZooKeeper to monitor new jobs.
   */
  private final JobMonitor jobMonitor;

  /**
   * Initializes this online runtime instance.
   *
   * @param name              Name.
   * @param eventHandler      Event handler.
   * @param persistentContext Persistent context.
   * @param openTelemetry     OpenTelemetry.
   * @param curatorFramework  CuratorFramework.
   */
  public OnlineRuntime(
      String name,
      EventHandler eventHandler,
      Context persistentContext,
      OpenTelemetry openTelemetry,
      CuratorFramework curatorFramework
  ) {
    super(name, eventHandler, persistentContext, openTelemetry);

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
    // TODO: Add additional conditions/rules

    final var jobDescriptionRuntimeName = job.getJobDescription().runtimeName;

    if (!name.equals(jobDescriptionRuntimeName)) {
      logger.info(
          "Found job for runtime '%s' which does not match this runtime's name '%s', ignoring".formatted(jobDescriptionRuntimeName, name));

      return;
    }

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
        final var serviceImplementationSelector = new RandomServiceImplementationSelector(
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
            logger.info("Creating persistent context variable '{}'", variable.name());

            persistentContext.create(variable.name(), variable.value());
          } catch (IOException e) {
            logger.error("Did not create persistent context variable '{}', possibly already exists: {}", variable.name(), e.getMessage());
          }
        });

        // Create instances, newInstances will also instantiate nested state machines
        final var instanceIds = newInstances(List.of(stateMachine), serviceImplementationSelector, null);

        // Assign local data from the job description if the job description contains any local data. Assign to the parent and nested state machines
        if (!job.getJobDescription().localData.isEmpty()) {
          for (final var instanceId : instanceIds) {
            final var stateMachineInstance = findInstance(instanceId)
                .orElseThrow(() -> new UnsupportedOperationException(
                    "State machine '%s' with id '%s' was not instantiated.".formatted(stateMachine.getName(), instanceId)));

            for (final var localData : job.getJobDescription().localData.entrySet()) {
              try {
                // Assign local data entry, evaluate the value as an expression
                final var valueExpression = ExpressionBuilder.from(new ExpressionDescription(localData.getValue())).build();

                stateMachineInstance.getExtent().setOrCreate(localData.getKey(), valueExpression.execute(stateMachineInstance.getExtent()));
              } catch (IOException | IllegalArgumentException e) {
                throw new UnsupportedOperationException(
                    "Could not assign value '%s' to local data variable '%s'".formatted(localData.getKey(), localData.getValue()), e);
              }
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
