package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public final class TimeoutActionCommand extends ActionCommand {

  public TimeoutActionCommand(Scope scope, TimeoutAction action, boolean isWhile) {
    super(scope, isWhile);
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    return null;
  }
}
