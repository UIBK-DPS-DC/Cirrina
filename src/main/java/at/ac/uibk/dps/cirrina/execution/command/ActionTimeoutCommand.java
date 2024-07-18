package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_ID;

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
  public List<ActionCommand> execute(String stateMachineId) throws UnsupportedOperationException {
    logging.logAction(this.timeoutAction.getName().isPresent() ? this.timeoutAction.getName().get(): "null", stateMachineId);
    Span span = tracing.initianlizeSpan("Timeout Action", tracer, null);
    tracing.addAttributes(Map.of(ATTR_STATE_MACHINE_ID, stateMachineId),span);
    try(Scope scope = span.makeCurrent()) {
      final var commandFactory = new CommandFactory(executionContext);

      return List.of(commandFactory.createActionCommand(timeoutAction.getAction()));
    } finally {
      span.end();
    }
  }
}
