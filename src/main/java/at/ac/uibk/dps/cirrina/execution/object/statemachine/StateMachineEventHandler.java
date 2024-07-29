package at.ac.uibk.dps.cirrina.execution.object.statemachine;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_EVENT_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_EVENT_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_NAME;

import at.ac.uibk.dps.cirrina.execution.aspect.logging.Logging;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.Tracing;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.event.EventHandler;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.util.Map;


public class StateMachineEventHandler {

  private final StateMachine stateMachine;

  private final EventHandler eventHandler;

  private final Logging logging = new Logging();
  private final Tracing tracing = new Tracing();
  private final Tracer tracer = tracing.initializeTracer("Event Handler");

  public StateMachineEventHandler(StateMachine stateMachine, EventHandler eventHandler) {
    this.stateMachine = stateMachine;
    this.eventHandler = eventHandler;
  }


  public void sendEvent(Event event, Span parentSpan) throws IOException {
    logging.logEventSending(event, stateMachine.getId(), stateMachine.getStateMachineClass().getName());

    Span span = tracing.initializeSpan("Sending Event " + event.getName(), tracer, parentSpan);
    tracing.addAttributes(Map.of(
        ATTR_STATE_MACHINE_ID, stateMachine.getId(),
        ATTR_STATE_MACHINE_NAME, stateMachine.getStateMachineClass().getName(),
        ATTR_EVENT_NAME, event.getName(),
        ATTR_EVENT_ID, event.getId()), span);


    try (Scope scope = span.makeCurrent()) {
      eventHandler.sendEvent(event, stateMachine.getStateMachineInstanceId().toString());
    } catch (IOException e) {
      tracing.recordException(e, span);
      logging.logExeption(stateMachine.getId().toString(), e, stateMachine.getStateMachineClass().getName());
      throw e;
    } finally {
      span.end();
    }
  }
}
