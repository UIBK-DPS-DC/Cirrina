package at.ac.uibk.dps.cirrina.lang.classes;

import jakarta.validation.constraints.NotNull;

public final class ExpressionClass {

  @NotNull
  public String expression;

  ExpressionClass(String expression) {
    this.expression = expression;
  }
}
