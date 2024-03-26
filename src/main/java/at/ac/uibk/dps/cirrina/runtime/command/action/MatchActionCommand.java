package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.ArrayList;
import java.util.List;

public final class MatchActionCommand extends ActionCommand {

  private final MatchAction matchAction;

  public MatchActionCommand(Scope scope, MatchAction matchAction, boolean isWhile) {
    super(scope, isWhile);

    this.matchAction = matchAction;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    final var commands = new ArrayList<Command>();

    var extent = scope.getExtent();

    try {
      var conditionValue = matchAction.getValue().execute(extent);

      for (var entry : matchAction.getCasee().entrySet()) {
        var caseValue = entry.getKey().execute(extent);
        var caseAction = entry.getValue();

        if (conditionValue == caseValue) {
          commands.add(ActionCommand.from(scope, caseAction, false));
        }
      }
    } catch (RuntimeException e) {
      throw RuntimeException.from("Could not execute match action: %s", e.getMessage());
    }

    return commands;
  }
}
