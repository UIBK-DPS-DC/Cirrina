package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_ACTION_TIMEOUT_COMMAND_EXECUTE;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.execution.object.action.TimeoutAction;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.List;

public final class ActionTimeoutCommand extends ActionCommand {

  private final TimeoutAction timeoutAction;

  ActionTimeoutCommand(ExecutionContext executionContext, TimeoutAction timeoutAction) {
    super(executionContext);

    this.timeoutAction = timeoutAction;
  }

  @Override
  public List<ActionCommand> execute(Tracer tracer, Span parentSpan) throws CirrinaException {
    // Create span
    final var span = tracer.spanBuilder(SPAN_ACTION_TIMEOUT_COMMAND_EXECUTE)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    try (final var scope = span.makeCurrent()) {
      final var commandFactory = new CommandFactory(executionContext);

      return List.of(commandFactory.createActionCommand(timeoutAction.getAction()));
    } finally {
      span.end();
    }
  }
}
