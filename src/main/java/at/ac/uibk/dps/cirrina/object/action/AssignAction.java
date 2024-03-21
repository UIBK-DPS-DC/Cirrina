package at.ac.uibk.dps.cirrina.object.action;

import at.ac.uibk.dps.cirrina.object.context.ContextVariable;
import java.util.Optional;

/**
 * Assign action, assigns a new value to a variable.
 */
public final class AssignAction extends Action {

  /**
   * The variable to create, is an expression. The name of the variable will be retrieved through evaluating the expression.
   */
  private final ContextVariable variable;

  /**
   * Initializes this assign action.
   *
   * @param name     Name, can be optional in which case this action is inline.
   * @param variable Context variable.
   * @throws IllegalArgumentException In case compilation of the expressions fails.
   */
  AssignAction(Optional<String> name, ContextVariable variable) throws IllegalArgumentException {
    super(name);

    this.variable = variable;
  }

  public ContextVariable getVariable() {
    return variable;
  }
}
