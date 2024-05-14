package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_ACTION_MATCH_COMMAND_EXECUTE;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.execution.object.action.MatchAction;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.ArrayList;
import java.util.List;

public final class ActionMatchCommand extends ActionCommand {

  private final MatchAction matchAction;

  ActionMatchCommand(ExecutionContext executionContext, MatchAction matchAction) {
    super(executionContext);

    this.matchAction = matchAction;
  }

  @Override
  public List<ActionCommand> execute(Tracer tracer, Span parentSpan) throws CirrinaException {
    // Create span
    final var span = tracer.spanBuilder(SPAN_ACTION_MATCH_COMMAND_EXECUTE)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    try (final var scope = span.makeCurrent()) {
      final var commands = new ArrayList<ActionCommand>();

      final var extent = executionContext.scope().getExtent();
      final var conditionValue = matchAction.getValue().execute(extent);

      final var commandFactory = new CommandFactory(executionContext);

      try {
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
      } catch (CirrinaException e) {
        throw CirrinaException.from("Could not execute match action actionCommand: %s", e.getMessage());
      }

      return commands;
    } finally {
      span.end();
    }
  }
}
