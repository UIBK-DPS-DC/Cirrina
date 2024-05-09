package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_VARIABLE_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_ACTION_CREATE_COMMAND_EXECUTE;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.core.object.expression.Expression;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.ArrayList;
import java.util.List;

public final class ActionCreateCommand extends ActionCommand {

  private final CreateAction createAction;

  ActionCreateCommand(ExecutionContext executionContext, CreateAction createAction) {
    super(executionContext);

    this.createAction = createAction;
  }

  @Override
  public List<ActionCommand> execute(Tracer tracer, Span parentSpan) throws CirrinaException {
    final var variable = createAction.getVariable();
    final var variableName = variable.name();

    // Create span
    final var span = tracer.spanBuilder(SPAN_ACTION_CREATE_COMMAND_EXECUTE)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    // Span attributes
    span.setAttribute(ATTR_VARIABLE_NAME, variableName);

    try (final var scope = span.makeCurrent()) {
      final var commands = new ArrayList<ActionCommand>();

      final var extent = executionContext.scope().getExtent();

      final var isPersistent = createAction.isPersistent();

      // If the variable should be created persistently, we assume that the lowest priority context in the extent is the persistent context,
      // if the variable should not be created persistently, we assume that the highest priority context in the extent is the relevant local context
      final var targetContext = isPersistent ?
          extent.getLow() : // The lowest priority context in the extent is the persistent context
          extent.getHigh(); // The highest priority context in the extent is the local context in scope

      // Create the variable
      try {
        // Acquire the value, in case the variable is lazy, we have to find the value through evaluating the value expression
        Object value = null;
        if (variable.isLazy()) {
          final var expression = variable.value();

          assert expression instanceof Expression;
          value = ((Expression) expression).execute(extent);
        } else {
          value = variable.value();
        }

        // Attempt to create the variable
        targetContext.create(variableName, value);
      } catch (CirrinaException e) {
        throw CirrinaException.from("Could not execute create action actionCommand: %s", e.getMessage());
      }

      return commands;
    } finally {
      span.end();
    }
  }
}
