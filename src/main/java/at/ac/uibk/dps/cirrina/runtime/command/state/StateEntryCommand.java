package at.ac.uibk.dps.cirrina.runtime.command.state;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.action.ActionCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * State entry command, provides a command that can execute a state entry. Note that entering the state is not performed inside this
 * command, instead, the state change should happen before execution of this command.
 *
 * @see at.ac.uibk.dps.cirrina.runtime.command.statemachine.StateChangeCommand
 */
public final class StateEntryCommand implements Command {

  /**
   * The current state.
   */
  private final StateInstance state;

  /**
   * Initializes this state entry command.
   *
   * @param state The current state.
   */
  public StateEntryCommand(StateInstance state) {
    this.state = state;
  }

  /**
   * Executes this state entry command. The execution produces the commands to execute when entering the current state.
   *
   * @param executionContext Execution context.
   * @return Commands to execute when entering the state.
   * @throws RuntimeException In case the command could not be executed.
   */
  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    // New commands
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
