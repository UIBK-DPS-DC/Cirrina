package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance.InstanceId;
import at.ac.uibk.dps.cirrina.runtime.scheduler.Scheduler;
import at.ac.uibk.dps.cirrina.runtime.scheduler.Scheduler.StateMachineInstanceCommand;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Runtime implements Runnable, EventListener {

  private static final Logger logger = LogManager.getLogger();

  private final Queue<StateMachineInstance> instances = new ConcurrentLinkedQueue<>();

  private final Scheduler scheduler;

  private final EventHandler eventHandler;

  private final Context persistentContext;

  private List<StateMachineInstanceCommandExecutionObserver> stateMachineInstanceCommandExecutionObservers = new ArrayList<>();

  public Runtime(Scheduler scheduler, EventHandler eventHandler, Context persistentContext) throws RuntimeException {
    this.scheduler = scheduler;
    this.eventHandler = eventHandler;

    this.persistentContext = persistentContext;
  }

  public InstanceId newInstance(StateMachine stateMachine) throws RuntimeException {
    final var instance = new StateMachineInstance(this, stateMachine, Optional.empty());
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
            Stream.iterate(scheduler.select(instances), Optional::isPresent, ignored -> scheduler.select(instances))
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
