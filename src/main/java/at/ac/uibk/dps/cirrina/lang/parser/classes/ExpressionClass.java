package at.ac.uibk.dps.cirrina.lang.parser.classes;

import jakarta.validation.constraints.NotNull;

public class ExpressionClass {

  @NotNull
  public String expression;

  ExpressionClass(String expression) {
    this.expression = expression;
  }
}
