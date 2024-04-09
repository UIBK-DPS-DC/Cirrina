package at.ac.uibk.dps.cirrina.core.object.guard;

import at.ac.uibk.dps.cirrina.core.object.expression.Expression;
import java.util.Optional;

public class Guard {

  private final Optional<String> name;
  private final Expression expression;

  public Guard(Optional<String> name, Expression expression) {
    this.name = name;
    this.expression = expression;
  }

  public Optional<String> getName() {
    return name;
  }

  public Expression getExpression() {
    return expression;
  }
}
