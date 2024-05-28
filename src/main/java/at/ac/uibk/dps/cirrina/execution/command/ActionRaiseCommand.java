package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_ACTION_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_ACTION_RAISE_COMMAND_EXECUTE;

import at.ac.uibk.dps.cirrina.csml.keyword.EventChannel;
import at.ac.uibk.dps.cirrina.execution.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class ActionRaiseCommand extends ActionCommand {

  private final RaiseAction raiseAction;

  private final AtomicReference<Double> latency = new AtomicReference<>();

  ActionRaiseCommand(ExecutionContext executionContext, RaiseAction raiseAction) {
    super(executionContext);

    this.raiseAction = raiseAction;
  }

  @Override
  public List<ActionCommand> execute(
      Tracer tracer,
      Span parentSpan,
      DoubleGauge latencyGauge
  ) throws UnsupportedOperationException {
    final var a = System.nanoTime() / 1.0e6;

    try {
      final var event = raiseAction.getEvent();

      // Create span
      final var span = tracer.spanBuilder(SPAN_ACTION_RAISE_COMMAND_EXECUTE)
          .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
          .startSpan();

      // Span attributes
      span.setAllAttributes(getAttributes());
      span.setAllAttributes(event.getAttributes());

      try (final var scope = span.makeCurrent()) {
        final var commands = new ArrayList<ActionCommand>();

        final var extent = executionContext.scope().getExtent();
        final var eventHandler = executionContext.eventHandler();
        final var eventListener = executionContext.eventListener();

        final var evaluatedEvent = Event.ensureHasEvaluatedData(event, extent);

        // Dispatch the event
        if (evaluatedEvent.getChannel() == EventChannel.INTERNAL) {
          eventListener.onReceiveEvent(evaluatedEvent);
        } else {
          // Send the event through the event handler
          eventHandler.sendEvent(evaluatedEvent);
        }

        // Record latency
        latencyGauge.set(System.nanoTime() / 1.0e6 - a);

        return commands;
      } finally {
        span.end();
      }
    } catch (Exception e) {
      throw new UnsupportedOperationException("Could not execute raise action", e);
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
        AttributeKey.stringKey(ATTR_ACTION_NAME), raiseAction.getName().orElse("")
    );
  }
}
