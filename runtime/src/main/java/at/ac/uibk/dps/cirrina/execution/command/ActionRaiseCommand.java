package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.lang.classes.event.EventChannel;
import at.ac.uibk.dps.cirrina.core.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import java.util.ArrayList;
import java.util.List;

public final class ActionRaiseCommand extends ActionCommand {

  private final RaiseAction raiseAction;

  ActionRaiseCommand(ExecutionContext executionContext, RaiseAction raiseAction) {
    super(executionContext);

    this.raiseAction = raiseAction;
  }

  @Override
  public List<ActionCommand> execute() throws CirrinaException {
    final var commands = new ArrayList<ActionCommand>();

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
        throw CirrinaException.from("Could not execute raise action actionCommand: %s", e.getMessage());
      }
    }

    return commands;
  }
}
