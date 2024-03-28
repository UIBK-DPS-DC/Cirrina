package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.core.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.core.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.core.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.core.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;

/**
 * Action command, a base class for commands that perform actions. Action commands are scoped, such that data retrieval happens with respect
 * to the scope of the action command. An action command can also be instructed to be executed while in a state.
 */
public abstract class ActionCommand implements Command {

  /**
   * The scope of execution.
   */
  protected final Scope scope;

  /**
   * Whether this action is to be executed while in a state.
   */
  protected final boolean isWhile;

  /**
   * Initializes this action command.
   *
   * @param scope   Execution scope.
   * @param isWhile Is a while action.
   */
  public ActionCommand(Scope scope, boolean isWhile) {
    this.scope = scope;
    this.isWhile = isWhile;
  }

  /**
   * Builder method, builds a specific action command, given an action.
   *
   * @param scope   Execution scope.
   * @param action  Action.
   * @param isWhile Is a while action.
   * @return Built action command.
   * @throws IllegalStateException If the action is not recognized.
   */
  public static Command from(Scope scope, Action action, boolean isWhile) throws IllegalStateException {
    switch (action) {
      case AssignAction a -> {
        return new AssignActionCommand(scope, a, isWhile);
      }
      case CreateAction a -> {
        return new CreateActionCommand(scope, a, isWhile);
      }
      case InvokeAction a -> {
        return new InvokeActionCommand(scope, a, isWhile);
      }
      case MatchAction a -> {
        return new MatchActionCommand(scope, a, isWhile);
      }
      case RaiseAction a -> {
        return new RaiseActionCommand(scope, a, isWhile);
      }
      case TimeoutAction a -> {
        return new TimeoutActionCommand(scope, a, isWhile);
      }
      case TimeoutResetAction a -> {
        return new TimeoutResetActionCommand(scope, a, isWhile);
      }
      default -> throw new IllegalStateException("Unexpected action");
    }
  }
}
