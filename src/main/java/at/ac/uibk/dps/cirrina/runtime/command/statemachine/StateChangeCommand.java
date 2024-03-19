package at.ac.uibk.dps.cirrina.runtime.command.statemachine;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import java.util.List;

public final class StateChangeCommand implements Command {

  private StateMachineInstance stateMachine;

  private StateInstance targetState;

  public StateChangeCommand(StateMachineInstance stateMachine, StateInstance targetState) throws RuntimeException {
    this.stateMachine = stateMachine;
    this.targetState = targetState;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    // Switch states
    stateMachine.setActiveState(targetState);

    return List.of();
  }
}
