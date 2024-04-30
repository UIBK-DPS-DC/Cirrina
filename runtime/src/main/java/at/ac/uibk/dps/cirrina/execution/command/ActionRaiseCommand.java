package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.lang.classes.event.EventChannel;
import at.ac.uibk.dps.cirrina.core.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.core.object.event.Event;

public final class ActionRaiseCommand extends Command {

  private final RaiseAction raiseAction;

  ActionRaiseCommand(ExecutionContext executionContext, RaiseAction raiseAction) {
    super(executionContext);

    this.raiseAction = raiseAction;
  }

  @Override
  public void execute() throws CirrinaException {
    final var extent = executionContext.scope().getExtent();

    final var eventHandler = executionContext.eventHandler();
    final var eventListener = executionContext.eventListener();
    final var event = Event.ensureHasEvaluatedData(raiseAction.getEvent(), extent);

    if (event.getChannel() == EventChannel.INTERNAL) {
      eventListener.onReceiveEvent(event);
    } else {
      try {
        // Send the event through the event handler
        eventHandler.sendEvent(event);
      } catch (CirrinaException e) {
        throw CirrinaException.from("Could not execute raise action command: %s", e.getMessage());
      }
    }
  }
}
