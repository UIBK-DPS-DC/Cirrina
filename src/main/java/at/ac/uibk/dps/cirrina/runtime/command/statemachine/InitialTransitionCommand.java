package at.ac.uibk.dps.cirrina.runtime.command.statemachine;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.state.StateEntryCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import java.util.ArrayList;
import java.util.List;

/**
 * Initial transition command, provides a command that can execute an initial transition. An initial transition is the transition into the
 * initial state of the state machine and should only occur once.
 */
public final class InitialTransitionCommand implements Command {

  /**
   * The target state.
   */
  private final StateInstance targetState;


  /**
   * Initializes this initial transition command.
   *
   * @param targetState The target state.
   */
  public InitialTransitionCommand(StateInstance targetState) throws RuntimeException {
    this.targetState = targetState;
  }

  /**
   * Executes this initial transition command. The execution produces exactly a state change command and state entry command, the commands
   * necessary to effectively transition into the initial state.
   *
   * @param executionContext Execution context.
   * @return Commands to execute when transitioning into the state.
   * @throws RuntimeException In case the command could not be executed.
   */
  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    // New commands
    final var commands = new ArrayList<Command>();

    // Change the active state
    commands.add(new StateChangeCommand(targetState));

    // Enter the target state
    commands.add(new StateEntryCommand(targetState));

    return commands;
  }
}
