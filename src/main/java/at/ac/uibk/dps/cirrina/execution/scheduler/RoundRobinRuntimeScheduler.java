package at.ac.uibk.dps.cirrina.execution.scheduler;

import at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine;
import java.util.List;
import java.util.Optional;

/**
 * Round-robin scheduler, schedules state machine executions in a round-robin pattern, finding the next state machine instance that can
 * execute a actionCommand.
 */
public final class RoundRobinRuntimeScheduler implements RuntimeScheduler {

  private int index = 0;

  @Override
  public Optional<StateMachine> select(List<StateMachine> instances) {
    /*// Select the first instance with a actionCommand in its queue
    for (var stateMachine : Iterables.limit(Iterables.skip(Iterables.cycle(instances), index), instances.size())) {
      ++index;

      if (stateMachine.canExecute()) {
        return Optional.of(stateMachine);
      }
    }*/

    return Optional.empty();
  }
}
