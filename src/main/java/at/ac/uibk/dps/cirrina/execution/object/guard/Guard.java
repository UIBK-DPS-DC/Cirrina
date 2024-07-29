package at.ac.uibk.dps.cirrina.execution.object.guard;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_GUARD_EXPRESSION;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_NAME;

import at.ac.uibk.dps.cirrina.execution.aspect.logging.Logging;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.Tracing;
import at.ac.uibk.dps.cirrina.execution.object.context.Extent;
import at.ac.uibk.dps.cirrina.execution.object.expression.Expression;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

/**
 * Guard, represents an evaluable guard that yields a boolean return value.
 */
public class Guard {

  /**
   * Guard name, only applicable for a named guard. An in-line guard can have null as its name.
   */
  private final @Nullable String name;

  /**
   * Guard expression.
   */
  private final Expression expression;

  private final Logging logging = new Logging();
  private final Tracing tracing = new Tracing();
  private final Tracer tracer = tracing.initializeTracer("Guard");

  /**
   * Initializes this guard object.
   * <p>
   * The name can be empty in case the guard is in-line.
   *
   * @param name       Name of the guard.
   * @param expression Expression of the guard.
   */
  Guard(@Nullable String name, Expression expression) {
    this.name = name;
    this.expression = expression;
  }

  /**
   * Evaluate this guard and return the resulting boolean value.
   *
   * @param extent Extent describing variables in scope.
   * @return Boolean result.
   * @throws UnsupportedOperationException If the guard expression could not be executed.
   * @throws IllegalArgumentException      If the expression could not be evaluated, or the expression does not produce a boolean value.
   */
  public boolean evaluate(Extent extent, String stateMachineId, String stateMachineName, Span parentSpan) throws IllegalArgumentException, UnsupportedOperationException {
    logging.logGuardEvaluation(expression.toString(), stateMachineName, stateMachineId);
    Span span = tracing.initializeSpan("Guard Evaluation: " + expression.toString(), tracer, parentSpan);
    tracing.addAttributes(Map.of(
        ATTR_GUARD_EXPRESSION, expression.toString(),
        ATTR_STATE_MACHINE_ID, stateMachineId,
        ATTR_STATE_MACHINE_NAME, stateMachineName), span);
    try(Scope scope = span.makeCurrent()) {
      var result = expression.execute(extent);

      try {
        if (!(result instanceof Boolean)) {
          throw new IllegalArgumentException("Guard expression '%s' does not produce a boolean value".formatted(expression));
        }
      } catch (IllegalArgumentException e) {
        tracing.recordException(e, span);
        logging.logExeption(stateMachineId, e, stateMachineName);
        throw e;
      }

      return (Boolean) result;
    }finally {
      span.end();
    }
  }

  /**
   * Returns the guard name.
   *
   * @return Guard name.
   */
  public Optional<String> getName() {
    return Optional.ofNullable(name);
  }

  /**
   * Returns the expression.
   *
   * @return Expression.
   */
  public Expression getExpression() {
    return expression;
  }
}
