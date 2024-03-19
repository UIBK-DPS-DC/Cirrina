package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public class InvokeActionCommand implements Command {

  public InvokeActionCommand(Scope scope, InvokeAction action, boolean isWhile) {

  }

  @Override
  public List<Command> execute() throws RuntimeException {
    return null;
  }
}
