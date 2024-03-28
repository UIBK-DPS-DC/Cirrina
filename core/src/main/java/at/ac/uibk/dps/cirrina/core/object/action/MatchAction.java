package at.ac.uibk.dps.cirrina.core.object.action;

import at.ac.uibk.dps.cirrina.core.object.expression.Expression;
import java.util.Map;
import java.util.Optional;

public final class MatchAction extends Action {

  private final Expression value;

  private final Map<Expression, Action> casee;

  MatchAction(Parameters parameters) {
    super(parameters.name());

    this.value = parameters.value();
    this.casee = parameters.casee();
  }

  public Expression getValue() {
    return value;
  }

  public Map<Expression, Action> getCasee() {
    return casee;
  }

  public record Parameters(
      Optional<String> name,
      Expression value,
      Map<Expression, Action> casee
  ) {

  }
}
