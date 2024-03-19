package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.context.Context;
import at.ac.uibk.dps.cirrina.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.runtime.StateMachineInstance.InstanceId;
import at.ac.uibk.dps.cirrina.runtime.scheduler.Scheduler;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Runtime<T extends Scheduler> implements Runnable {

  private static final Logger logger = LogManager.getLogger();

  private final Queue<StateMachineInstance> instances = new ConcurrentLinkedQueue<>();

  private final T scheduler;

  private final Context persistentContext;

  public Runtime(Class<T> schedulerClass, Context persistentContext) throws RuntimeException {
    // Instantiate the scheduler
    try {
      this.scheduler = schedulerClass.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw RuntimeException.from("Failed to acquire a scheduler instance: %s", e.getMessage());
    }

    this.persistentContext = persistentContext;
  }

  public InstanceId newInstance(StateMachine stateMachine) throws RuntimeException {
    var instance = new StateMachineInstance(this, stateMachine);
    instances.add(instance);

    return instance.instanceId;
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
}
