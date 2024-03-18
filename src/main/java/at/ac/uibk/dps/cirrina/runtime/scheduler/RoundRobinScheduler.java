package at.ac.uibk.dps.cirrina.runtime.scheduler;

import at.ac.uibk.dps.cirrina.runtime.StateMachineInstance;
import com.google.common.collect.Iterables;
import java.util.Optional;
import java.util.Queue;

/**
 * Round-robin scheduler, schedules state machine executions in a round-robin pattern, finding the next state machine instance that can
 * execute a command.
 */
public class RoundRobinScheduler implements Scheduler {

  private int index = 0;

  /**
   * Returns a state machine instance command whenever a next state machine can be found within the queue of instances that can execute a
   * command. A state machine instance can execute a command when it is not locked, and it has a command in its queue.
   * <p>
   * State machines that are locked are skipped: if the instance queue contains {SM1, SM2, SM3, SM4} and SM1 is locked, SM2 is unlocked and
   * has an executable command, then SM2's executable command is returned and SM2 will be locked. On the next invocation of this function,
   * SM3 is evaluated.
   *
   * @param instances Instance queue.
   * @return State machine instance command or none if no executable command was found.
   */
  @Override
  public Optional<StateMachineInstanceCommand> select(Queue<StateMachineInstance> instances) {
    // Select the first instance with a command in its queue
    for (var stateMachine : Iterables.limit(Iterables.skip(Iterables.cycle(instances), index), instances.size())) {
      ++index;

      // Attempt to get an executable command
      var command = stateMachine.getExecutableCommand();
      if (command.isPresent()) {
        return Optional.of(new StateMachineInstanceCommand(stateMachine, command.get()));
      }
    }

    return Optional.empty();
  }
}
