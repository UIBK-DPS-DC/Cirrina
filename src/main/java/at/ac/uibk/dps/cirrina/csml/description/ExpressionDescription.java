package at.ac.uibk.dps.cirrina.csml.description;

import jakarta.validation.constraints.NotNull;

public final class ExpressionDescription {

  @NotNull
  public String expression;

  public ExpressionDescription(String expression) {
    this.expression = expression;
  }
}
