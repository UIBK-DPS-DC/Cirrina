package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.execution.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.tracing.TracingAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Map;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.*;

public final class ActionTimeoutCommand extends ActionCommand {

  private final TimeoutAction timeoutAction;

  ActionTimeoutCommand(ExecutionContext executionContext, TimeoutAction timeoutAction) {
    super(executionContext);

    this.timeoutAction = timeoutAction;
  }

  @Override
  public List<ActionCommand> execute(TracingAttributes tracingAttributes, Span parentSpan) throws UnsupportedOperationException {
    logging.logAction(timeoutAction.getName(), tracingAttributes.getStateMachineId(), tracingAttributes.getStateMachineName());
    Span span = tracing.initializeSpan("Timeout Action", tracer, parentSpan,
            Map.of( ATTR_STATE_MACHINE_ID, tracingAttributes.getStateMachineId(),
                    ATTR_STATE_MACHINE_NAME, tracingAttributes.getStateMachineName(),
                    ATTR_PARENT_STATE_MACHINE_ID, tracingAttributes.getParentStateMachineId(),
                    ATTR_PARENT_STATE_MACHINE_NAME, tracingAttributes.getParentStateMachineName()));
    try(Scope scope = span.makeCurrent()) {
      final var commandFactory = new CommandFactory(executionContext);

      return List.of(commandFactory.createActionCommand(timeoutAction.getAction(), tracingAttributes, span));
    } finally {
      span.end();
    }
  }
}
