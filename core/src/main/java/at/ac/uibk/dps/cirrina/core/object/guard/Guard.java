package at.ac.uibk.dps.cirrina.core.object.guard;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.expression.Expression;
import java.util.Optional;

/**
 * Guard, represents an evaluable guard that yields a boolean return value.
 */
public class Guard {

  private final Optional<String> name;
  private final Expression expression;

  /**
   * Initializes this guard object.
   * <p>
   * The name can be empty in case the guard is in-line.
   *
   * @param name       Name of the guard.
   * @param expression Expression of the guard.
   */
  Guard(Optional<String> name, Expression expression) {
    this.name = name;
    this.expression = expression;
  }

  /**
   * Returns the guard name.
   *
   * @return Guard name.
   */
  public Optional<String> getName() {
    return name;
  }

  /**
   * Returns the expression.
   *
   * @return Expression.
   */
  public Expression getExpression() {
    return expression;
  }

  /**
   * Evaluate this guard and return the resulting boolean value.
   *
   * @param extent Extent describing variables in scope.
   * @return Boolean result.
   * @throws RuntimeException If the expression could not be evaluated, or the expression does not produce a boolean value.
   */
  public boolean evaluate(Extent extent) throws RuntimeException {
    var result = expression.execute(extent);
    if (!(result instanceof Boolean)) {
      throw RuntimeException.from("Guard expression '%s' does not produce a boolean value", expression);
    }

    return (Boolean) result;
  }
}
