package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public final class MatchActionCommand implements Command {

  private Scope scope;

  private MatchAction matchAction;

  public MatchActionCommand(Scope scope, MatchAction matchAction) {
    this.scope = scope;
    this.matchAction = matchAction;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    return null;
  }
}
