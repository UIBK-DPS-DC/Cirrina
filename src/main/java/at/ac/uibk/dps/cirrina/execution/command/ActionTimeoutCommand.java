package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_ACTION_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_ACTION_TIMEOUT_COMMAND_EXECUTE;

import at.ac.uibk.dps.cirrina.execution.object.action.TimeoutAction;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
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
  public List<ActionCommand> execute(
      Tracer tracer,
      Span parentSpan,
      DoubleGauge latencyGauge
  ) throws UnsupportedOperationException {
    // Create span
    final var span = tracer.spanBuilder(SPAN_ACTION_TIMEOUT_COMMAND_EXECUTE)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    // Span attributes
    span.setAllAttributes(getAttributes());

    try (final var scope = span.makeCurrent()) {
      final var commandFactory = new CommandFactory(executionContext);

      return List.of(commandFactory.createActionCommand(timeoutAction.getAction()));
    } finally {
      span.end();
    }
  }

  /**
   * Get OpenTelemetry attributes of this state machine.
   *
   * @return Attributes.
   */
  @Override
  public Attributes getAttributes() {
    return Attributes.of(
        AttributeKey.stringKey(ATTR_ACTION_NAME), timeoutAction.getName().orElse("")
    );
  }
}
