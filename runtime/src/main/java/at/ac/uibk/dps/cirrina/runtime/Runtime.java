package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstanceId;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RuntimeScheduler;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract runtime, provides the generic runtime behavior of running state machine instances. The abstract runtime is specialized as a
 * shared runtime, providing the non-distributed execution of a collaborative state machine. The distributed runtime is a specialization of
 * the abstract runtime that provides the distributed execution of a set of state machine instances.
 *
 * @see SharedRuntime
 * @see DistributedRuntime
 */
public abstract class Runtime implements Runnable, EventListener {

  /**
   * The runtime logger.
   */
  private static final Logger logger = LogManager.getLogger();

  /**
   * Lock for state machine instance collection.
   */
  private final ReentrantLock stateMachineInstancesLock = new ReentrantLock();

  /**
   * The collection of executed state machine instances.
   */
  private final List<StateMachineInstance> stateMachineInstances = new ArrayList<>();

  /**
   * The runtime runtimeScheduler.
   */
  private final RuntimeScheduler runtimeScheduler;

  /**
   * The event handler.
   */
  private final EventHandler eventHandler;

  /**
   * The persistent context.
   */
  private final Context persistentContext;

  public Runtime(RuntimeScheduler runtimeScheduler, EventHandler eventHandler, Context persistentContext) throws CirrinaException {
    this.runtimeScheduler = runtimeScheduler;
    this.eventHandler = eventHandler;

    this.persistentContext = persistentContext;
  }

  protected StateMachineInstanceId newInstance(StateMachine stateMachine) throws CirrinaException {
    return newInstance(stateMachine, Optional.empty());
  }

  protected StateMachineInstanceId newInstance(StateMachine stateMachine, Optional<StateMachineInstanceId> parentId)
      throws CirrinaException {
    stateMachineInstancesLock.lock();

    try {
      // Find the actual parent state machine instance
      final var parentInstance = parentId.flatMap(this::findInstance);

      if (parentInstance.isEmpty() && parentId.isPresent()) {
        throw CirrinaException.from("The parent state machine instance with id '%s' could not be found", parentId.get());
      }

      // Create the state machine instance
      final var instance = new StateMachineInstance(this, stateMachine, parentInstance.orElse(null));

      stateMachineInstances.add(instance);

      eventHandler.addListener(instance);

      return instance.getStateMachineInstanceId();
    } finally {
      stateMachineInstancesLock.unlock();
    }
  }

  /**
   * Runs this operation.
   */
  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        // Critical section
        stateMachineInstancesLock.lock();

        try {
          // Select commands until no command to be executed can be found anymore
          for (final var stateMachineInstanceCommand : runtimeScheduler.select(stateMachineInstances)) {
            final var stateMachineInstance = stateMachineInstanceCommand.stateMachineInstance();
            final var command = stateMachineInstanceCommand.command();

            stateMachineInstance.execute(command);
          }
        } finally {
          stateMachineInstancesLock.unlock();
        }

        // TODO: Remove me (re-add notify/wait)
        Thread.sleep(1);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Find a state machine instance based on its instance id.
   *
   * @param stateMachineInstanceId State machine instance id.
   * @return The state machine instance or an empty optional if no state machine instance was found for the given instance id.
   */
  public Optional<StateMachineInstance> findInstance(StateMachineInstanceId stateMachineInstanceId) {
    // Critical section
    stateMachineInstancesLock.lock();

    try {
      return stateMachineInstances.stream()
          .filter(stateMachineInstance -> stateMachineInstance.getStateMachineInstanceId().equals(stateMachineInstanceId))
          .findFirst();
    } finally {
      stateMachineInstancesLock.unlock();
    }
  }

  public Extent getExtent() {
    return new Extent(persistentContext);
  }

  public EventHandler getEventHandler() {
    return eventHandler;
  }
}
