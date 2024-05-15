package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_VARIABLE_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_ACTION_ASSIGN_COMMAND_EXECUTE;

import at.ac.uibk.dps.cirrina.execution.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.execution.object.expression.Expression;
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
  public List<ActionCommand> execute(Tracer tracer, Span parentSpan) throws UnsupportedOperationException {
    try {
      final var variable = assignAction.getVariable();
      final var variableName = variable.name();

      // Create span
      final var span = tracer.spanBuilder(SPAN_ACTION_ASSIGN_COMMAND_EXECUTE)
          .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
          .startSpan();

      // Span attributes
      span.setAttribute(ATTR_VARIABLE_NAME, variableName);

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

        return commands;
      } finally {
        span.end();
      }
    } catch (Exception e) {
      throw new UnsupportedOperationException("Could not execute assign action", e);
    }
  }
}
