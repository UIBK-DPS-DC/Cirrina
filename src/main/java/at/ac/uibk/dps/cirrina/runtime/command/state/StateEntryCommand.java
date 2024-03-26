package at.ac.uibk.dps.cirrina.runtime.command.state;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.action.ActionCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class StateEntryCommand implements Command {

  private final StateInstance state;

  public StateEntryCommand(StateInstance state) {
    this.state = state;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    final var commands = new ArrayList<Command>();

    // Append the entry actions to the command list
    new TopologicalOrderIterator<>(state.getState().getEntry()).forEachRemaining(
        action -> commands.add(ActionCommand.from(state, action, false)));

    // Append the while actions to the command list
    new TopologicalOrderIterator<>(state.getState().getWhile()).forEachRemaining(
        action -> commands.add(ActionCommand.from(state, action, true)));

    // TODO: Start timeout actions

    return commands;
  }
}
