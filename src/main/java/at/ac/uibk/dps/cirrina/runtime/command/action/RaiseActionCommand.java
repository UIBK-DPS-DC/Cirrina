package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public class RaiseActionCommand implements Command {

  public RaiseActionCommand(Scope scope, RaiseAction action, boolean isWhile) {

  }

  @Override
  public List<Command> execute() throws RuntimeException {
    return null;
  }
}
