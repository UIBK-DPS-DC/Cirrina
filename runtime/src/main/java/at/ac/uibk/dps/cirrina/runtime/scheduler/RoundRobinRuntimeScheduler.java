package at.ac.uibk.dps.cirrina.runtime.scheduler;

import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import java.util.List;
import java.util.Optional;

/**
 * Round-robin scheduler, schedules state machine executions in a round-robin pattern, finding the next state machine instance that can
 * execute a actionCommand.
 */
public final class RoundRobinRuntimeScheduler implements RuntimeScheduler {

  private int index = 0;

  @Override
  public Optional<StateMachineInstance> select(List<StateMachineInstance> instances) {
    /*// Select the first instance with a actionCommand in its queue
    for (var stateMachineInstance : Iterables.limit(Iterables.skip(Iterables.cycle(instances), index), instances.size())) {
      ++index;

      if (stateMachineInstance.canExecute()) {
        return Optional.of(stateMachineInstance);
      }
    }*/

    return Optional.empty();
  }
}
