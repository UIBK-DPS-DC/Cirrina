package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public final class TimeoutActionCommand implements Command {

  public TimeoutActionCommand(Scope scope, TimeoutAction action) {

  }

  @Override
  public List<Command> execute() throws RuntimeException {
    return null;
  }
}
