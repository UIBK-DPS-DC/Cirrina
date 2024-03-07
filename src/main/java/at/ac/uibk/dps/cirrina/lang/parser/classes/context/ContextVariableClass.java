package at.ac.uibk.dps.cirrina.lang.parser.classes.context;

import at.ac.uibk.dps.cirrina.lang.parser.classes.ExpressionClass;
import jakarta.validation.constraints.NotNull;

public class ContextVariableClass {

  /**
   * The name.
   * <p>
   * Can be referenced in an expression.
   * </p>
   */
  @NotNull
  public String name;

  /**
   * The value.
   */
  @NotNull
  public ExpressionClass value;
}
