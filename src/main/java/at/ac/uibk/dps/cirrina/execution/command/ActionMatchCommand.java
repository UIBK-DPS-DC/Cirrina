package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.execution.object.action.MatchAction;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.ArrayList;
import java.util.List;

public final class ActionMatchCommand extends ActionCommand {

  private final MatchAction matchAction;

  ActionMatchCommand(ExecutionContext executionContext, MatchAction matchAction) {
    super(executionContext);

    this.matchAction = matchAction;
  }

  @Override
  public List<ActionCommand> execute() throws UnsupportedOperationException {
    logging.logAction(this.matchAction.getName().isPresent() ? this.matchAction.getName().get() : "null");
    Span span = tracing.initianlizeSpan("Match Action", tracer, null);
    try(Scope scope = span.makeCurrent()) {
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
      logging.logExeption(e);
      tracing.recordException(e, span);
      throw new UnsupportedOperationException("Could not execute match action", e);
    } finally {
      span.end();
    }
  }
}
