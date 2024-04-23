package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.ArrayList;
import java.util.List;

public final class TimeoutActionCommand extends ActionCommand {

  private final TimeoutAction timeoutAction;

  public TimeoutActionCommand(Scope scope, TimeoutAction timeoutAction, boolean isWhile) {
    super(scope, isWhile);

    this.timeoutAction = timeoutAction;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws CirrinaException {
    final var commands = new ArrayList<Command>();

    final var stateMachineInstance = executionContext.stateMachineInstance();

    commands.add(ActionCommand.from(stateMachineInstance, this.timeoutAction.getAction(), false));

    return commands;
  }
}
