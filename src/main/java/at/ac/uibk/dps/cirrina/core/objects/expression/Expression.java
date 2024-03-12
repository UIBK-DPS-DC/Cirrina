package at.ac.uibk.dps.cirrina.core.objects.expression;

import at.ac.uibk.dps.cirrina.core.CoreException;
import at.ac.uibk.dps.cirrina.core.objects.context.Context;

/**
 * Expression, re presents an executable expression that yields a return value.
 */
public abstract class Expression {

  /**
   * Executes this expression, producing a value.
   *
   * @param context Context containing variables in scope.
   * @return Result of the expression.
   * @throws CoreException In case of an error while executing the expression.
   */
  public abstract Object execute(Context context) throws CoreException;
}
