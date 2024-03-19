package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public class MatchActionCommand implements Command {

  public MatchActionCommand(Scope scope, MatchAction action, boolean isWhile) {

  }

  @Override
  public List<Command> execute() throws RuntimeException {
    return null;
  }
}
