package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_EVENT_CHANNEL;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_EVENT_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_EVENT_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_ACTION_RAISE_COMMAND_EXECUTE;

import at.ac.uibk.dps.cirrina.csml.keyword.EventChannel;
import at.ac.uibk.dps.cirrina.execution.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.ArrayList;
import java.util.List;

public final class ActionRaiseCommand extends ActionCommand {

  private final RaiseAction raiseAction;

  ActionRaiseCommand(ExecutionContext executionContext, RaiseAction raiseAction) {
    super(executionContext);

    this.raiseAction = raiseAction;
  }

  @Override
  public List<ActionCommand> execute(Tracer tracer, Span parentSpan) throws UnsupportedOperationException {
    try {
      final var event = raiseAction.getEvent();

      // Create span
      final var span = tracer.spanBuilder(SPAN_ACTION_RAISE_COMMAND_EXECUTE)
          .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
          .startSpan();

      // Span attributes
      span.setAttribute(ATTR_EVENT_ID, event.getId());
      span.setAttribute(ATTR_EVENT_NAME, event.getName());
      span.setAttribute(ATTR_EVENT_CHANNEL, event.getChannel().toString());

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

        return commands;
      } finally {
        span.end();
      }
    } catch (Exception e) {
      throw new UnsupportedOperationException("Could not execute raise action", e);
    }
  }
}
