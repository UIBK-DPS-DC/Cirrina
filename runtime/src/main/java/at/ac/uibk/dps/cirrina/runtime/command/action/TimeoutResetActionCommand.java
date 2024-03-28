package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public final class TimeoutResetActionCommand extends ActionCommand {

  public TimeoutResetActionCommand(Scope scope, TimeoutResetAction action, boolean isWhile) {
    super(scope, isWhile);
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    return null;
  }
}
