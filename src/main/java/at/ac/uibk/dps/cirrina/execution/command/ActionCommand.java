package at.ac.uibk.dps.cirrina.execution.command;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.List;

/**
 * ActionCommand, represents the interface to commands that can be entered into a state machine's actionCommand queue. Commands can be
 * executed and may produce new commands and have side effects.
 */
public abstract class ActionCommand {

  protected final ExecutionContext executionContext;

  public ActionCommand(ExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  public abstract List<ActionCommand> execute(
      Tracer tracer,
      Span parentSpan,
      DoubleGauge latencyGauge
  ) throws UnsupportedOperationException;

  /**
   * Get OpenTelemetry attributes of this state machine.
   *
   * @return Attributes.
   */
  public abstract Attributes getAttributes();
}
