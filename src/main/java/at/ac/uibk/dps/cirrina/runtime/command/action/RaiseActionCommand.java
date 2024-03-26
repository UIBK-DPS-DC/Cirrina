package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.object.event.Event;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public final class RaiseActionCommand extends ActionCommand {

  private final RaiseAction raiseAction;

  public RaiseActionCommand(Scope scope, RaiseAction raiseAction, boolean isWhile) {
    super(scope, isWhile);

    this.raiseAction = raiseAction;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    var extent = scope.getExtent();
    var eventHandler = scope.getEventHandler();

    var source = executionContext.stateMachineInstance().getId().toString();

    var event = Event.ensureHasEvaluatedData(raiseAction.event, extent);

    eventHandler.sendEvent(event, source);

    return null;
  }
}
