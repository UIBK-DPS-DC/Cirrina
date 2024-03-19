package at.ac.uibk.dps.cirrina.runtime.command.state;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.action.ActionCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class StateEntryCommand implements Command {

  private StateInstance stateInstance;

  public StateEntryCommand(StateInstance stateInstance) {
    this.stateInstance = stateInstance;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    var commands = new ArrayList<Command>();

    // Then add the entry actions, executed in topological order:
    // Append the entry actions to the command list
    new TopologicalOrderIterator<>(stateInstance.getState().getEntry()).forEachRemaining(
        action -> commands.add(ActionCommand.from(stateInstance, action, false)));

    // Append the while actions to the command list
    new TopologicalOrderIterator<>(stateInstance.getState().getWhile()).forEachRemaining(
        action -> commands.add(ActionCommand.from(stateInstance, action, true)));

    // TODO: Start timeout actions

    return commands;
  }
}
