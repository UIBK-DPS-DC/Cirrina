package at.ac.uibk.dps.cirrina.core.runtime.command.statemachine;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.runtime.command.Command;
import at.ac.uibk.dps.cirrina.core.runtime.instance.StateInstance;
import java.util.List;

public final class StateChangeCommand implements Command {

  private final StateInstance targetState;

  public StateChangeCommand(StateInstance targetState) throws RuntimeException {
    this.targetState = targetState;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    // Switch states
    executionContext.stateMachineInstance().setActiveState(targetState);

    return List.of();
  }
}
