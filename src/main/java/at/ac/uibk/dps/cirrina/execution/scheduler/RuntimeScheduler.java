package at.ac.uibk.dps.cirrina.execution.scheduler;

import at.ac.uibk.dps.cirrina.execution.command.ActionCommand;
import at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine;
import java.util.List;
import java.util.Optional;

public interface RuntimeScheduler {

  Optional<StateMachine> select(List<StateMachine> instances);

  record StateMachineInstanceCommand(StateMachine stateMachine, ActionCommand actionCommand) {

  }
}
