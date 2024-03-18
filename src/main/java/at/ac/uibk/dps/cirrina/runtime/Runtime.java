package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.runtime.StateMachineInstance.InstanceId;
import at.ac.uibk.dps.cirrina.runtime.scheduler.Scheduler;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public final class Runtime<T extends Scheduler> implements Runnable {

  private final Queue<StateMachineInstance> instances = new ConcurrentLinkedQueue<>();

  private final T scheduler;

  public Runtime(Class<T> schedulerClass) throws RuntimeException {
    // Instantiate the scheduler
    try {
      this.scheduler = schedulerClass.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw RuntimeException.from("Failed to acquire a scheduler instance: %s", e.getMessage());
    }
  }

  public InstanceId newInstance(StateMachine stateMachine) {
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

                CompletableFuture.runAsync(() -> instance.execute(command));
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
