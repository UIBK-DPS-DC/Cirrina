package at.ac.uibk.dps.cirrina.core.object.action;

import at.ac.uibk.dps.cirrina.core.object.builder.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.core.object.expression.Expression;
import java.util.Optional;

/**
 * Assign action, assigns a new value to a variable.
 */
public final class AssignAction extends Action {

  /**
   * The variable to assign to, is an expression. The name of the variable will be retrieved through evaluating the expression.
   */
  public final Expression variable;

  /**
   * The value to assign, is an expression. The value will be retrieved through evaluating the expression.
   */
  public final Expression value;

  /**
   * Initializes this assign action.
   *
   * @param name     Name, can be optional in which case this action is inline.
   * @param variable Variable expression source.
   * @param value    Value expression source.
   * @throws IllegalArgumentException In case compilation of the expressions fails.
   */
  public AssignAction(Optional<String> name, String variable, String value) throws IllegalArgumentException {
    super(name);

    this.variable = ExpressionBuilder.from(variable).build();
    this.value = ExpressionBuilder.from(value).build();
  }
}
