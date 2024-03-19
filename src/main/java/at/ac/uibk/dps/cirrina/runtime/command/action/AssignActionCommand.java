package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public class AssignActionCommand implements Command {

  public AssignActionCommand(Scope scope, AssignAction action, boolean isWhile) {

  }

  @Override
  public List<Command> execute() throws RuntimeException {
    return null;
  }
}
