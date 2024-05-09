package at.ac.uibk.dps.cirrina.execution.scheduler;

import at.ac.uibk.dps.cirrina.execution.command.ActionCommand;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import java.util.List;
import java.util.Optional;

public interface RuntimeScheduler {

  Optional<StateMachineInstance> select(List<StateMachineInstance> instances);

  record StateMachineInstanceCommand(StateMachineInstance stateMachineInstance, ActionCommand actionCommand) {

  }
}
