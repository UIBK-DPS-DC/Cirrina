package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.execution.aspect.logging.LogAction;
import at.ac.uibk.dps.cirrina.execution.aspect.logging.LogGeneral;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.TracesGeneral;
import at.ac.uibk.dps.cirrina.execution.object.action.MatchAction;
import java.util.ArrayList;
import java.util.List;

public final class ActionMatchCommand extends ActionCommand {

  private final MatchAction matchAction;

  ActionMatchCommand(ExecutionContext executionContext, MatchAction matchAction) {
    super(executionContext);

    this.matchAction = matchAction;
  }

  @TracesGeneral
  @LogGeneral
  @LogAction
  @Override
  public List<ActionCommand> execute() throws UnsupportedOperationException {
    try {
      final var commands = new ArrayList<ActionCommand>();

      final var extent = executionContext.scope().getExtent();
      final var conditionValue = matchAction.getValue().execute(extent);

      final var commandFactory = new CommandFactory(executionContext);

      // Find matching conditions and append the commands to the set of new commands
      for (var entry : matchAction.getCase().entrySet()) {
        final var caseValue = entry.getKey().execute(extent);
        final var caseAction = entry.getValue();

        // In case the case condition matches, add the case action
        if (conditionValue == caseValue) {
          final var command = commandFactory.createActionCommand(caseAction);

          commands.add(command);
        }
      }

      return commands;
    } catch (Exception e) {
      throw new UnsupportedOperationException("Could not execute match action", e);
    }
  }
}
