package at.ac.uibk.dps.cirrina.runtime.command.statemachine;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.state.StateEntryCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import java.util.ArrayList;
import java.util.List;

public final class InitialTransitionCommand implements Command {

  private final StateInstance targetState;

  public InitialTransitionCommand(StateInstance targetState) throws RuntimeException {
    this.targetState = targetState;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    final var commands = new ArrayList<Command>();

    // Change the active state
    commands.add(new StateChangeCommand(targetState));

    // Enter the target state
    commands.add(new StateEntryCommand(targetState));

    return commands;
  }
}
