package at.ac.uibk.dps.cirrina.execution.object.statemachine;

import at.ac.uibk.dps.cirrina.execution.aspect.logging.Logging;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.Tracing;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.tracing.TracingAttributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.util.Map;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.*;


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


  public void sendEvent(Event event, TracingAttributes tracingAttributes, Span parentSpan) throws IOException {
    logging.logEventSending(event, tracingAttributes.getStateMachineId(), tracingAttributes.getStateMachineName());

    Span span = tracing.initializeSpan("Sending Event " + event.getName(), tracer, parentSpan,
            Map.of( ATTR_STATE_MACHINE_ID, tracingAttributes.getStateMachineId(),
                    ATTR_STATE_MACHINE_NAME, tracingAttributes.getStateMachineName(),
                    ATTR_PARENT_STATE_MACHINE_ID, tracingAttributes.getParentStateMachineId(),
                    ATTR_PARENT_STATE_MACHINE_NAME, tracingAttributes.getParentStateMachineName(),
                    ATTR_EVENT_NAME, event.getName(),
                    ATTR_EVENT_ID, event.getId()));


    try (Scope scope = span.makeCurrent()) {
      eventHandler.sendEvent(event, stateMachine.getStateMachineInstanceId().toString());
    } catch (IOException e) {
      tracing.recordException(e, span);
      logging.logExeption(tracingAttributes.getStateMachineId(), e, tracingAttributes.getStateMachineName());
      throw e;
    } finally {
      span.end();
    }
  }
}
