package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.core.object.expression.Expression;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

/**
 * Create action command, provides a command that can execute a create action.
 */
public final class CreateActionCommand extends ActionCommand {

  /**
   * Create action.
   */
  private final CreateAction createAction;

  /**
   * Initializes this create action command.
   *
   * @param scope        Execution scope.
   * @param createAction Action.
   * @param isWhile      Is a while action.
   */
  public CreateActionCommand(Scope scope, CreateAction createAction, boolean isWhile) {
    super(scope, isWhile);

    this.createAction = createAction;
  }

  /**
   * Executes this assign action command. After execution, the variable described in the create action will have been created, given the
   * value of the create action value.
   * <p>
   * This command produces no new commands.
   *
   * @param executionContext Execution context.
   * @return Empty list.
   * @throws CirrinaException In case the command could not be executed.
   */
  @Override
  public List<Command> execute(ExecutionContext executionContext) throws CirrinaException {
    // Acquire the is persistent flag
    final var isPersistent = createAction.isPersistent();

    // Acquire the extent
    final var extent = scope.getExtent();

    // Acquire the variable
    final var variable = createAction.getVariable();

    // If the variable should be created persistently, we assume that the lowest priority context in the extent is the persistent context,
    // if the variable should not be created persistently, we assume that the highest priority context in the extent is the relevant local context
    final var targetContext = isPersistent ?
        extent.getLow() : // The lowest priority context in the extent is the persistent context
        extent.getHigh(); // The highest priority context in the extent is the local context in scope

    // Create the variable
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

      // Attempt to create the variable
      targetContext.create(variable.name(), value);
    } catch (CirrinaException e) {
      throw CirrinaException.from("Could not execute create action command: %s", e.getMessage());
    }

    return List.of();
  }
}
