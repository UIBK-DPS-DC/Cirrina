package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.object.expression.Expression;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public final class AssignActionCommand extends ActionCommand {

  private final AssignAction assignAction;

  public AssignActionCommand(Scope scope, AssignAction assignAction, boolean isWhile) {
    super(scope, isWhile);

    this.assignAction = assignAction;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    var extent = scope.getExtent();

    var variable = assignAction.getVariable();

    // Acquire the value
    Object value = null;
    if (variable.isLazy()) {
      var expression = variable.value();

      assert expression instanceof Expression;
      value = ((Expression) expression).execute(extent);
    } else {
      value = variable.value();
    }

    extent.trySet(variable.name(), value);

    return null;
  }
}
