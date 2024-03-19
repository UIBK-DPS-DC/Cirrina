package at.ac.uibk.dps.cirrina.runtime.command.statemachine;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import java.util.List;

public final class StateChangeCommand implements Command {

  private StateMachineInstance stateMachineInstance;

  private State targetState;

  public StateChangeCommand(StateMachineInstance stateMachineInstance, State targetState) throws RuntimeException {
    this.stateMachineInstance = stateMachineInstance;
    this.targetState = targetState;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    // Switch states
    stateMachineInstance.setActiveStateByName(targetState.getName());

    return List.of();
  }
}
