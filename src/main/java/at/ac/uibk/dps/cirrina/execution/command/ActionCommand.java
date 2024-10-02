package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.execution.aspect.logging.Logging;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.Tracing;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.List;

/**
 * ActionCommand, represents the interface to commands that can be entered into a state machine's actionCommand queue. Commands can be
 * executed and may produce new commands and have side effects.
 */
public abstract class ActionCommand {

  protected final Tracing tracing = new Tracing();
  protected final Tracer tracer = tracing.initializeTracer("Action Command");
  protected final Logging logging = new Logging();

  protected final ExecutionContext executionContext;

  public ActionCommand(ExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  public abstract List<ActionCommand> execute(String stateMachineId, String stateMachineName, String parentStateMachineId, String parentStateMachineName, Span parentSpan) throws UnsupportedOperationException;
}
