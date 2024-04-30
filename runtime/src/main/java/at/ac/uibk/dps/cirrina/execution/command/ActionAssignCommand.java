package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.core.object.expression.Expression;

public final class ActionAssignCommand extends Command {

  private final AssignAction assignAction;

  ActionAssignCommand(ExecutionContext executionContext, AssignAction assignAction) {
    super(executionContext);

    this.assignAction = assignAction;
  }

  @Override
  public void execute() throws CirrinaException {
    final var extent = executionContext.scope().getExtent();

    final var variable = assignAction.getVariable();

    try {
      // Acquire the value, in case the variable is lazy, we have to find the value through evaluating the value expression
      Object value = null;
      if (variable.isLazy()) {
        final var expression = variable.value();

        assert expression instanceof Expression;
        value = ((Expression) expression).execute(extent);
      } else {
        value = variable.value();
      }

      // Attempt to set the variable
      extent.trySet(variable.name(), value);
    } catch (CirrinaException e) {
      throw CirrinaException.from("Could not execute assign action command: %s", e.getMessage());
    }
  }
}
