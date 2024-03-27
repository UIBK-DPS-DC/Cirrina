package at.ac.uibk.dps.cirrina.runtime.command.state;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.action.ActionCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * State exit command, provides a command that can execute a state exit. Note that exiting the state is not performed inside this command,
 * instead, the state change should follow after execution of this command.
 *
 * @see at.ac.uibk.dps.cirrina.runtime.command.statemachine.StateChangeCommand
 */
public final class StateExitCommand implements Command {

  /**
   * The current state.
   */
  private final StateInstance state;

  /**
   * Initializes this state exit command.
   *
   * @param state The current state.
   */
  public StateExitCommand(StateInstance state) {
    this.state = state;
  }

  /**
   * Executes this state exit command. The execution produces the commands to execute when exiting the current state.
   *
   * @param executionContext Execution context.
   * @return Commands to execute when exiting the state.
   * @throws RuntimeException In case the command could not be executed.
   */
  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    // New commands
    final var commands = new ArrayList<Command>();

    // Append the entry actions to the command list
    new TopologicalOrderIterator<>(state.getState().getExit()).forEachRemaining(
        action -> commands.add(ActionCommand.from(state, action, false)));

    // TODO: Cancel timeout and while actions

    return commands;
  }
}
