package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.object.expression.Expression;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public final class CreateActionCommand extends ActionCommand {

  private final CreateAction createAction;

  public CreateActionCommand(Scope scope, CreateAction createAction, boolean isWhile) {
    super(scope, isWhile);

    this.createAction = createAction;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    final var isPersistent = createAction.isPersistent();

    var extent = scope.getExtent();

    // Create the variable
    try {
      var targetContext = isPersistent ?
          extent.getLow() : // The lowest priority context in the extent is the persistent context
          extent.getHigh(); // The highest priority context in the extent is the local context in scope

      var variable = createAction.getVariable();

      // Acquire the value
      Object value = null;
      if (variable.isLazy()) {
        var expression = variable.value();

        assert expression instanceof Expression;
        value = ((Expression) expression).execute(extent);
      } else {
        value = variable.value();
      }

      targetContext.create(variable.name(), value);
    } catch (RuntimeException e) {
      throw RuntimeException.from("Could not execute create action: %s", e.getMessage());
    }

    return null;
  }
}
