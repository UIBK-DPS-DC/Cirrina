package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.execution.object.action.TimeoutAction;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.List;

public final class ActionTimeoutCommand extends ActionCommand {

  private final TimeoutAction timeoutAction;

  ActionTimeoutCommand(ExecutionContext executionContext, TimeoutAction timeoutAction) {
    super(executionContext);

    this.timeoutAction = timeoutAction;
  }

  @Override
  public List<ActionCommand> execute() throws UnsupportedOperationException {
    logging.logAction(this.timeoutAction.getName().isPresent() ? this.timeoutAction.getName().get(): "null");
    Span span = tracing.initianlizeSpan("Timeout Action", tracer, null);
    try(Scope scope = span.makeCurrent()) {
      final var commandFactory = new CommandFactory(executionContext);

      return List.of(commandFactory.createActionCommand(timeoutAction.getAction()));
    } finally {
      span.end();
    }
  }
}
