package at.ac.uibk.dps.cirrina.runtime.base;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance.InstanceId;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RuntimeScheduler;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RuntimeScheduler.StateMachineInstanceCommand;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract runtime, provides the generic runtime behavior of running state machine instances. The abstract runtime is specialized as a
 * shared runtime, providing the non-distributed execution of a collaborative state machine. The distributed runtime is a specialization of
 * the abstract runtime that provides the distributed execution of a set of state machine instances.
 *
 * @see at.ac.uibk.dps.cirrina.runtime.shared.SharedRuntime
 * @see at.ac.uibk.dps.cirrina.runtime.distributed.DistributedRuntime
 */
public abstract class Runtime implements Runnable, EventListener {

  /**
   * The runtime logger.
   */
  private static final Logger logger = LogManager.getLogger();

  /**
   * The collection of executed state machine instances.
   */
  private final Queue<StateMachineInstance> instances = new ConcurrentLinkedQueue<>();

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

  private List<StateMachineInstanceCommandExecutionObserver> stateMachineInstanceCommandExecutionObservers = new ArrayList<>();

  public Runtime(RuntimeScheduler runtimeScheduler, EventHandler eventHandler, Context persistentContext) throws RuntimeException {
    this.runtimeScheduler = runtimeScheduler;
    this.eventHandler = eventHandler;

    this.persistentContext = persistentContext;
  }

  protected InstanceId newInstance(StateMachine stateMachine) throws RuntimeException {
    return newInstance(stateMachine, Optional.empty());
  }

  protected InstanceId newInstance(StateMachine stateMachine, Optional<InstanceId> parentId) throws RuntimeException {

    // Find the actual parent state machine instance
    final var parentInstance = parentId.flatMap(this::findInstance);
    if (parentInstance.isEmpty() && parentId.isPresent()) {
      throw RuntimeException.from("The parent state machine instance with id '%s' could not be found", parentId.get());
    }

    // Create the state machine instance
    final var instance = new StateMachineInstance(this, stateMachine, parentInstance);

    instances.add(instance);

    eventHandler.addListener(instance);

    return instance.getId();
  }

  /**
   * Runs this operation.
   */
  @Override
  public void run() {
    try (ExecutorService executor = Executors.newFixedThreadPool(1)) {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          synchronized (this) {
            // Consume the currently executable commands
            Stream.iterate(runtimeScheduler.select(instances), Optional::isPresent, ignored -> runtimeScheduler.select(instances))
                .map(Optional::get)
                .forEach(instanceCommand -> {
                  var instance = instanceCommand.instance();
                  var command = instanceCommand.command();

                  executor.submit(() -> {
                    try {
                      stateMachineInstanceCommandExecutionObservers
                          .forEach(observer -> observer.update(instanceCommand));

                      instance.execute(command);
                    } catch (RuntimeException e) {
                      logger.error("Runtime error while executing command: {}", e.getMessage());
                    }
                  });
                });

            // Wait until awoken
            wait();
          }
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  /**
   * Find a state machine instance based on its instance id.
   *
   * @param instanceId State machine instance id.
   * @return The state machine instance or an empty optional if no state machine instance was found for the given instance id.
   */
  public Optional<StateMachineInstance> findInstance(InstanceId instanceId) {
    return instances.stream()
        .filter(instance -> instance.getId().equals(instanceId))
        .findFirst();
  }

  public Extent getExtent() {
    return new Extent(persistentContext);
  }

  public EventHandler getEventHandler() {
    return eventHandler;
  }

  interface StateMachineInstanceCommandExecutionObserver {

    void update(StateMachineInstanceCommand stateMachineInstanceCommand);
  }
}
