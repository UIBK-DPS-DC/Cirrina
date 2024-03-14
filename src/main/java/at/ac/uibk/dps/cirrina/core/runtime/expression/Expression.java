package at.ac.uibk.dps.cirrina.core.runtime.expression;

import at.ac.uibk.dps.cirrina.core.runtime.RuntimeException;
import at.ac.uibk.dps.cirrina.core.runtime.context.Context;

/**
 * Expression, represents an executable expression that yields a return value.
 */
public abstract class Expression {

  /**
   * The expression source string.
   */
  public final String source;

  /**
   * Initializes the base expression.
   *
   * @param source Source string.
   */
  Expression(String source) {
    this.source = source;
  }

  /**
   * Executes this expression, producing a value.
   *
   * @param context Context containing variables in scope.
   * @return Result of the expression.
   * @throws RuntimeException In case of an error while executing the expression.
   */
  public abstract Object execute(Context context) throws RuntimeException;
}
