package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public class TimeoutResetActionCommand implements Command {

  public TimeoutResetActionCommand(Scope scope, TimeoutResetAction action, boolean isWhile) {

  }

  @Override
  public List<Command> execute() throws RuntimeException {
    return null;
  }
}
