package at.ac.uibk.dps.cirrina.core.runtime.command.action;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.core.runtime.command.Command;
import java.util.List;

public final class InvokeActionCommand extends ActionCommand {

  private final InvokeAction invokeAction;

  public InvokeActionCommand(Scope scope, InvokeAction invokeAction, boolean isWhile) {
    super(scope, isWhile);

    this.invokeAction = invokeAction;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    return null;
  }
}
