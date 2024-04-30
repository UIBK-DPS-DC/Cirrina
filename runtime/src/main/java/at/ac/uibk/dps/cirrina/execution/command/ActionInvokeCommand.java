package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.InvokeAction;

public final class ActionInvokeCommand extends Command {

  private final InvokeAction invokeAction;

  ActionInvokeCommand(ExecutionContext executionContext, InvokeAction invokeAction) {
    super(executionContext);

    this.invokeAction = invokeAction;
  }

  @Override
  public void execute() throws CirrinaException {
  }
}
