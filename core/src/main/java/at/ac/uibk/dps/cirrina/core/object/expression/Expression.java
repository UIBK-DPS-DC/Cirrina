package at.ac.uibk.dps.cirrina.core.object.expression;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;

/**
 * Expression, represents an executable expression that yields a return value.
 */
public abstract class Expression {

  /**
   * The expression source string.
   */
  private final String source;

  /**
   * Initializes the base expression.
   *
   * @param source Source string.
   */
  Expression(String source) {
    this.source = source;
  }

  public String getSource() {
    return source;
  }

  /**
   * Executes this expression, producing a value.
   *
   * @param extent Extent describing variables in scope.
   * @return Result of the expression.
   * @throws CirrinaException In case of an error while executing the expression.
   */
  public abstract Object execute(Extent extent) throws CirrinaException;

  @Override
  public String toString() {
    return source;
  }
}
