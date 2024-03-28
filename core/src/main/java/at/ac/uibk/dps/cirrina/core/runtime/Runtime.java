package at.ac.uibk.dps.cirrina.core.runtime;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.core.runtime.command.Command.Scope;
import at.ac.uibk.dps.cirrina.core.runtime.instance.StateMachineInstance;
import at.ac.uibk.dps.cirrina.core.runtime.instance.StateMachineInstance.InstanceId;
import at.ac.uibk.dps.cirrina.core.runtime.scheduler.Scheduler;
import java.util.EventListener;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Runtime implements Runnable, Scope, EventListener {

  private static final Logger logger = LogManager.getLogger();

  private final Queue<StateMachineInstance> instances = new ConcurrentLinkedQueue<>();

  private final Context localContext = new InMemoryContext();

  private final Scheduler scheduler;

  private final EventHandler eventHandler;

  private final Context persistentContext;

  public Runtime(Scheduler scheduler, EventHandler eventHandler, Context persistentContext) throws RuntimeException {
    this.scheduler = scheduler;
    this.eventHandler = eventHandler;

    this.persistentContext = persistentContext;
  }

  public InstanceId newInstance(StateMachine stateMachine) throws RuntimeException {
    final var instance = new StateMachineInstance(this, stateMachine, Optional.empty());
    instances.add(instance);

    return instance.getId();
  }

  /**
   * Runs this operation.
   */
  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        synchronized (this) {
          // Consume the currently executable commands
          Stream.iterate(scheduler.select(instances), Optional::isPresent, ignored -> scheduler.select(instances))
              .map(Optional::get)
              .forEach(instanceCommand -> {
                var instance = instanceCommand.instance();
                var command = instanceCommand.command();

                CompletableFuture.runAsync(() -> {
                  try {
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

  @Override
  public Extent getExtent() {
    return new Extent(persistentContext, localContext);
  }

  public EventHandler getEventHandler() {
    return null;
  }
}
