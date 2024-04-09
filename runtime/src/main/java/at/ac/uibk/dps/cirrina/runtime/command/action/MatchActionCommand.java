package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.ArrayList;
import java.util.List;

/**
 * Match action command, provides a command that can execute a match action.
 */
public final class MatchActionCommand extends ActionCommand {

  /**
   * Match action.
   */
  private final MatchAction matchAction;

  /**
   * Initializes this match action command.
   *
   * @param scope       Execution scope.
   * @param matchAction Action.
   * @param isWhile     Is a while action.
   */
  public MatchActionCommand(Scope scope, MatchAction matchAction, boolean isWhile) {
    super(scope, isWhile);

    this.matchAction = matchAction;
  }

  /**
   * Executes this match action command. The condition value is evaluated and compared against every condition value. If conditions match,
   * this commands will return the resulting commands.
   * <p>
   * This command produces new commands for which the condition value matches the case value.
   *
   * @param executionContext Execution context.
   * @return Empty list.
   * @throws RuntimeException In case the command could not be executed.
   */
  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    // New commands
    final var commands = new ArrayList<Command>();

    // Acquire the extent
    final var extent = scope.getExtent();

    // Acquire the condition value
    final var conditionValue = matchAction.getValue().execute(extent);

    try {
      // Find matching conditions and append the commands to the set of new commands
      for (var entry : matchAction.getCase().entrySet()) {
        final var caseValue = entry.getKey().execute(extent);
        final var caseAction = entry.getValue();

        if (conditionValue == caseValue) {
          commands.add(ActionCommand.from(scope, caseAction, false));
        }
      }
    } catch (RuntimeException e) {
      throw RuntimeException.from("Could not execute match action command: %s", e.getMessage());
    }

    return commands;
  }
}
