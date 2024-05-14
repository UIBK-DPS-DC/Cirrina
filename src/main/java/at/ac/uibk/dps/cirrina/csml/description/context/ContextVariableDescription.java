package at.ac.uibk.dps.cirrina.csml.description.context;

import at.ac.uibk.dps.cirrina.csml.description.ExpressionDescription;
import jakarta.validation.constraints.NotNull;

public final class ContextVariableDescription {

  /**
   * The name.
   * <p>
   * Can be referenced in an expression.
   */
  @NotNull
  public String name;

  /**
   * The value.
   */
  @NotNull
  public ExpressionDescription value;
}
