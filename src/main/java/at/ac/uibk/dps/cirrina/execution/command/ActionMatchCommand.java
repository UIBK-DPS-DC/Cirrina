package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_NAME;

import at.ac.uibk.dps.cirrina.execution.object.action.MatchAction;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ActionMatchCommand extends ActionCommand {

  private static final Logger logger = LogManager.getLogger();

  private final MatchAction matchAction;

  ActionMatchCommand(ExecutionContext executionContext, MatchAction matchAction) {
    super(executionContext);

    this.matchAction = matchAction;
  }

  @Override
  public List<ActionCommand> execute(String stateMachineId, String stateMachineName, Span parentSpan) throws UnsupportedOperationException {
    logging.logAction(matchAction.toString(), stateMachineId, stateMachineName);
    Span span = tracing.initializeSpan("Match Action", tracer, parentSpan);
    tracing.addAttributes(Map.of(
        ATTR_STATE_MACHINE_ID, stateMachineId,
        ATTR_STATE_MACHINE_NAME, stateMachineName), span);
    final var commands = new ArrayList<ActionCommand>();

    try(Scope scope = span.makeCurrent()) {
      final var extent = executionContext.scope().getExtent();
      final var conditionValue = matchAction.getValue().execute(extent);

      final var commandFactory = new CommandFactory(executionContext);

      // Find matching conditions and append the commands to the set of new commands
      for (var entry : matchAction.getCase().entrySet()) {
        final var caseValue = entry.getKey().execute(extent);
        final var caseAction = entry.getValue();

        // In case the case condition matches, add the case action
        if (conditionValue == caseValue) {
          final var command = commandFactory.createActionCommand(caseAction, span);

          commands.add(command);
        }
      }
    } catch (UnsupportedOperationException e) {
      logging.logExeption(stateMachineId, e, stateMachineName);
      tracing.recordException(e, span);
      logger.error("Could not execute match action: {}", e.getMessage());
    } finally {
      span.end();
    }

    return commands;
  }
}
