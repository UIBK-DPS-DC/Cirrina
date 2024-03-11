package at.ac.uibk.dps.cirrina.core.objects.actions;

import at.ac.uibk.dps.cirrina.core.objects.Expression;
import java.util.Optional;

public class AssignAction extends Action {

  public final Expression variable;

  public final Expression value;

  public AssignAction(Optional<String> name, String variable, String value)
      throws IllegalArgumentException {
    super(name);

    this.variable = new Expression(variable);
    this.value = new Expression(value);
  }
}
