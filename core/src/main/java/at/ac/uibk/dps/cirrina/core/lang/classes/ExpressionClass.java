package at.ac.uibk.dps.cirrina.core.lang.classes;

import jakarta.validation.constraints.NotNull;

public final class ExpressionClass {

  @NotNull
  public String expression;

  public ExpressionClass(String expression) {
    this.expression = expression;
  }
}
