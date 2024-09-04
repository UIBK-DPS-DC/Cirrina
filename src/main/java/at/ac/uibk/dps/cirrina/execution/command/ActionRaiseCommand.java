package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_NAME;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.EventChannel;
import at.ac.uibk.dps.cirrina.execution.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ActionRaiseCommand extends ActionCommand {

  private static final Logger logger = LogManager.getLogger();

  private final RaiseAction raiseAction;

  private final AtomicReference<Double> latency = new AtomicReference<>();

  ActionRaiseCommand(ExecutionContext executionContext, RaiseAction raiseAction) {
    super(executionContext);

    this.raiseAction = raiseAction;
  }

  @Override
  public List<ActionCommand> execute(String stateMachineId, String stateMachineName, Span parentSpan) throws UnsupportedOperationException {
    logging.logAction(raiseAction.toString() + "(" + raiseAction.getEvent().getId() + ")", stateMachineId, stateMachineName);
    Span span = tracing.initializeSpan("Raise Action", tracer, parentSpan);
    tracing.addAttributes(Map.of(
        ATTR_STATE_MACHINE_ID, stateMachineId,
        ATTR_STATE_MACHINE_NAME, stateMachineName),span);
    final var commands = new ArrayList<ActionCommand>();

    try(Scope scope = span.makeCurrent()) {
      final var event = raiseAction.getEvent();

      final var extent = executionContext.scope().getExtent();
      final var eventHandler = executionContext.eventHandler();
      final var eventListener = executionContext.eventListener();

      final var evaluatedEvent = Event.ensureHasEvaluatedData(event, extent);

      // Dispatch the event
      if (evaluatedEvent.getChannel() == EventChannel.INTERNAL) {
        eventListener.onReceiveEvent(evaluatedEvent, span);
      } else {
        // Send the event through the event handler
        eventHandler.sendEvent(evaluatedEvent, span);
      }
    } catch (IOException e) {
      logging.logExeption(stateMachineId, e, stateMachineName);
      tracing.recordException(e, span);
      logger.error("Data creation failed: {}", e.getMessage());
    } finally {
      span.end();
    }

    return commands;
  }
}
