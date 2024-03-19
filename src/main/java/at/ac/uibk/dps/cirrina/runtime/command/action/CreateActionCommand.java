package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public class CreateActionCommand implements Command {

  public CreateActionCommand(Scope scope, CreateAction action, boolean isWhile) {

  }

  @Override
  public List<Command> execute() throws RuntimeException {
    return null;
  }
}
