package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.object.action.Action;
import at.ac.uibk.dps.cirrina.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.object.action.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.Command.Scope;

public class ActionCommand {

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