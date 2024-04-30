package at.ac.uibk.dps.cirrina.runtime.scheduler;

import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;

/**
 * Round-robin scheduler, schedules state machine executions in a round-robin pattern, finding the next state machine instance that can
 * execute a command.
 */
public final class RoundRobinRuntimeScheduler implements RuntimeScheduler {

  private int index = 0;

  @Override
  public List<StateMachineInstanceCommand> select(List<StateMachineInstance> instances) {
    final var stateMachineInstanceCommands = new ArrayList<StateMachineInstanceCommand>();

    // Select the first instance with a command in its queue
    for (var stateMachineInstance : Iterables.limit(Iterables.skip(Iterables.cycle(instances), index), instances.size())) {
      ++index;

      // Add all executable commands to the list
      stateMachineInstance.takeNextCommand()
          .ifPresent(commands -> commands
              .forEach(command -> stateMachineInstanceCommands.add(new StateMachineInstanceCommand(stateMachineInstance, command))));
    }

    return stateMachineInstanceCommands;
  }
}
