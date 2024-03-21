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
   * @param parameters Initialization parameters.
   * @throws IllegalArgumentException In case compilation of the expressions fails.
   */
  AssignAction(Parameters parameters) throws IllegalArgumentException {
    super(parameters.name());

    this.variable = parameters.variable();
  }

  public ContextVariable getVariable() {
    return variable;
  }

  public record Parameters(
      Optional<String> name,
      ContextVariable variable
  ) {

  }
}
