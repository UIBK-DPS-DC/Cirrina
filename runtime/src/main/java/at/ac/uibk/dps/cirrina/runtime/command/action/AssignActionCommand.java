package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.core.object.expression.Expression;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

/**
 * Assign action command, provides a command that can execute an assignment action.
 */
public final class AssignActionCommand extends ActionCommand {

  /**
   * Assignment action.
   */
  private final AssignAction assignAction;

  /**
   * Initializes this assign action command.
   *
   * @param scope        Execution scope.
   * @param assignAction Action.
   * @param isWhile      Is a while action.
   */
  public AssignActionCommand(Scope scope, AssignAction assignAction, boolean isWhile) {
    super(scope, isWhile);

    this.assignAction = assignAction;
  }

  /**
   * Executes this assign action command. After execution, the variable described in the assign action will have been written to, given the
   * value of the assign action value.
   * <p>
   * This command produces no new commands.
   *
   * @param executionContext Execution context.
   * @return Empty list.
   * @throws RuntimeException In case the command could not be executed.
   */
  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    // Acquire the extent
    var extent = scope.getExtent();

    // Acquire the variable
    var variable = assignAction.getVariable();

    try {
      // Acquire the value, in case the variable is lazy, we have to find the value through evaluating the value expression
      Object value = null;
      if (variable.isLazy()) {
        var expression = variable.value();

        assert expression instanceof Expression;
        value = ((Expression) expression).execute(extent);
      } else {
        value = variable.value();
      }

      // Attempt to set the variable
      extent.trySet(variable.name(), value);
    } catch (RuntimeException e) {
      throw RuntimeException.from("Could not execute assign action command: %s", e.getMessage());
    }

    return List.of();
  }
}
