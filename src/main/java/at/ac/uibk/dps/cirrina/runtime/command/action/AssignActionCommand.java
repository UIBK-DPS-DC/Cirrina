package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.object.expression.Expression;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public final class AssignActionCommand implements Command {

  private Scope scope;

  private AssignAction assignAction;

  public AssignActionCommand(Scope scope, AssignAction assignAction) {
    this.scope = scope;
    this.assignAction = assignAction;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    var extent = scope.getExtent();

    var variable = assignAction.getVariable();

    // Acquire the value
    Object value = null;
    if (variable.isLazy()) {
      var expression = variable.value();

      assert expression instanceof Expression;
      value = ((Expression) expression).execute(scope.getExtent());
    } else {
      value = variable.value();
    }

    extent.trySet(variable.name(), value);

    return null;
  }
}
