package at.ac.uibk.dps.cirrina.execution.object.statemachine;

import at.ac.uibk.dps.cirrina.execution.aspect.logging.Logging;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.Tracing;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.event.EventHandler;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;


public class StateMachineEventHandler {

  private final StateMachine stateMachine;

  private final EventHandler eventHandler;

  private final Logging logging = new Logging();
  private final Tracing tracing = new Tracing();
  private final Tracer tracer = tracing.initializeTracer("Action");

  public StateMachineEventHandler(StateMachine stateMachine, EventHandler eventHandler) {
    this.stateMachine = stateMachine;
    this.eventHandler = eventHandler;
  }


  public void sendEvent(Event event) throws IOException {
    logging.logEventSending(event);
    Span span = tracing.initianlizeSpan("Sending Event", tracer, null);
    try (Scope scope = span.makeCurrent()) {
      eventHandler.sendEvent(event, stateMachine.getStateMachineInstanceId().toString());
    } catch (IOException e) {
      tracing.recordException(e, span);
      logging.logExeption(e);
      throw e;
    } finally {
      span.end();
    }
  }
}
