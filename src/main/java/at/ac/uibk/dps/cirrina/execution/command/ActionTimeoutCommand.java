package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.*;
import at.ac.uibk.dps.cirrina.execution.object.action.TimeoutAction;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Map;

public final class ActionTimeoutCommand extends ActionCommand {

  private final TimeoutAction timeoutAction;

  ActionTimeoutCommand(ExecutionContext executionContext, TimeoutAction timeoutAction) {
    super(executionContext);

    this.timeoutAction = timeoutAction;
  }

  @Override
  public List<ActionCommand> execute(String stateMachineId, String stateMachineName, String parentStateMachineId, String parentStateMachineName, Span parentSpan) throws UnsupportedOperationException {
    logging.logAction(timeoutAction.getName(), stateMachineId, stateMachineName);
    Span span = tracing.initializeSpan(
        "Timeout Action", tracer, parentSpan,
        Map.of(ATTR_STATE_MACHINE_ID, stateMachineId,
               ATTR_STATE_MACHINE_NAME, stateMachineName,
               ATTR_PARENT_STATE_MACHINE_ID, parentStateMachineId,
               ATTR_PARENT_STATE_MACHINE_NAME, parentStateMachineName));

    try(Scope scope = span.makeCurrent()) {
      final var commandFactory = new CommandFactory(executionContext);

      return List.of(commandFactory.createActionCommand(timeoutAction.getAction(), span, stateMachineName, stateMachineId, parentStateMachineName, parentStateMachineId));
    } finally {
      span.end();
    }
  }
}
