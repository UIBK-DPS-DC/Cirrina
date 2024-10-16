package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.*;

import at.ac.uibk.dps.cirrina.execution.aspect.logging.Logging;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.Tracing;
import at.ac.uibk.dps.cirrina.execution.object.action.Action;
import at.ac.uibk.dps.cirrina.execution.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.execution.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.execution.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.execution.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.execution.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.execution.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.tracing.TracingAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.util.Map;

public class CommandFactory {

  private ExecutionContext executionContext;

  protected final Tracing tracing = new Tracing();
  protected final Tracer tracer = tracing.initializeTracer("Action Factory");
  protected final Logging logging = new Logging();

  public CommandFactory(ExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  public ActionCommand createActionCommand(Action action, TracingAttributes tracingAttributes, Span parentSpan) {
    logging.logActionCreation(action.toString());
    Span span = tracing.initializeSpan("Action Factory " + action.toString(), tracer, parentSpan,
            Map.of( ATTR_STATE_MACHINE_ID,tracingAttributes.getStateMachineId(),
                    ATTR_STATE_MACHINE_NAME, tracingAttributes.getStateMachineName(),
                    ATTR_PARENT_STATE_MACHINE_ID, tracingAttributes.getParentStateMachineId(),
                    ATTR_PARENT_STATE_MACHINE_NAME, tracingAttributes.getParentStateMachineName()));
    try (Scope scope = span.makeCurrent()) {
      switch (action) {
        case AssignAction assignAction -> {
          return new ActionAssignCommand(executionContext, assignAction);
        }
        case CreateAction createAction -> {
          return new ActionCreateCommand(executionContext, createAction);
        }
        case InvokeAction invokeAction -> {
          return new ActionInvokeCommand(executionContext, invokeAction);
        }
        case MatchAction matchAction -> {
          return new ActionMatchCommand(executionContext, matchAction);
        }
        case RaiseAction raiseAction -> {
          return new ActionRaiseCommand(executionContext, raiseAction);
        }
        case TimeoutAction timeoutAction -> {
          return new ActionTimeoutCommand(executionContext, timeoutAction);
        }
        default -> throw new IllegalArgumentException("Unexpected action");
      }
    } catch (IllegalArgumentException e) {
      tracing.recordException(e, span);
      throw e;
    } finally {
      span.end();
    }

  }
}
