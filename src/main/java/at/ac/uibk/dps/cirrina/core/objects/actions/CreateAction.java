package at.ac.uibk.dps.cirrina.core.objects.actions;

import at.ac.uibk.dps.cirrina.core.objects.context.Context.ContextVariable;
import java.util.Optional;

/**
 * Create action, creates a new variable.
 */
public final class CreateAction extends Action {

  /**
   * The variable to create, is an expression. The name of the variable will be retrieved through evaluating the expression.
   */
  public final ContextVariable variable;

  /**
   * Indicates whether this variable should be created persistently.
   */
  public final boolean isPersistent;

  /**
   * Initializes this create action.
   *
   * @param name         Name, can be optional in which case this action is inline.
   * @param variable     Context variable.
   * @param isPersistent True if the variable should be created persistently, otherwise non-persistent.
   * @throws IllegalArgumentException In case compilation of the expressions fails.
   */
  public CreateAction(Optional<String> name, ContextVariable variable, boolean isPersistent) {
    super(name);

    this.variable = variable;
    this.isPersistent = isPersistent;
  }
}
