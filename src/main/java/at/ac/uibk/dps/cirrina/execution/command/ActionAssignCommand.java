package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_ACTION_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_VARIABLE_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_ACTION_ASSIGN_COMMAND_EXECUTE;

import at.ac.uibk.dps.cirrina.execution.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.execution.object.expression.Expression;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.ArrayList;
import java.util.List;

public final class ActionAssignCommand extends ActionCommand {

  private final AssignAction assignAction;

  ActionAssignCommand(ExecutionContext executionContext, AssignAction assignAction) {
    super(executionContext);

    this.assignAction = assignAction;
  }

  @Override
  public List<ActionCommand> execute(
      Tracer tracer,
      Span parentSpan,
      DoubleGauge latencyGauge
  ) throws UnsupportedOperationException {
    final var a = System.nanoTime() / 1.0e6;

    try {
      final var variable = assignAction.getVariable();
      final var variableName = variable.name();

      // Create span
      final var span = tracer.spanBuilder(SPAN_ACTION_ASSIGN_COMMAND_EXECUTE)
          .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
          .startSpan();

      // Span attributes
      span.setAllAttributes(getAttributes());

      try (final var scope = span.makeCurrent()) {
        final var commands = new ArrayList<ActionCommand>();

        final var extent = executionContext.scope().getExtent();

        // Acquire the value, in case the variable is lazy, we have to find the value through evaluating the value expression
        Object value = null;
        if (variable.isLazy()) {
          final var expression = variable.value();

          assert expression instanceof Expression;
          value = ((Expression) expression).execute(extent);
        } else {
          value = variable.value();
        }

        // Attempt to set the variable
        extent.trySet(variableName, value);

        // Record latency
        latencyGauge.set(System.nanoTime() / 1.0e6 - a);

        return commands;
      } finally {
        span.end();
      }
    } catch (Exception e) {
      throw new UnsupportedOperationException("Could not execute assign action", e);
    }
  }

  /**
   * Get OpenTelemetry attributes of this state machine.
   *
   * @return Attributes.
   */
  @Override
  public Attributes getAttributes() {
    return Attributes.of(
        AttributeKey.stringKey(ATTR_ACTION_NAME), assignAction.getName().orElse(""),
        AttributeKey.stringKey(ATTR_VARIABLE_NAME), assignAction.getVariable().name()
    );
  }
}
