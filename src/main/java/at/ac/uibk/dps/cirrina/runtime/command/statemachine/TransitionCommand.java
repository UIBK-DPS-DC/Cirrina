package at.ac.uibk.dps.cirrina.runtime.command.statemachine;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import java.util.List;

public class TransitionCommand implements Command {

  private StateMachineInstance stateMachineInstance;

  private State targetState;

  public TransitionCommand(StateMachineInstance stateMachineInstance, State targetState) throws RuntimeException {
    this.stateMachineInstance = stateMachineInstance;
    this.targetState = targetState;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    // TODO: Add exit previous state
    // TODO: Add transition actions
    // TODO: Add change to next state
    // TODO: Add enter next state

    return List.of();
  }
}
