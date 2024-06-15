package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.csml.keyword.EventChannel;
import at.ac.uibk.dps.cirrina.execution.aspect.logging.LogAction;
import at.ac.uibk.dps.cirrina.execution.aspect.logging.LogGeneral;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.TracesGeneral;
import at.ac.uibk.dps.cirrina.execution.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class ActionRaiseCommand extends ActionCommand {

  private final RaiseAction raiseAction;

  private final AtomicReference<Double> latency = new AtomicReference<>();

  ActionRaiseCommand(ExecutionContext executionContext, RaiseAction raiseAction) {
    super(executionContext);

    this.raiseAction = raiseAction;
  }

  @TracesGeneral
  @LogGeneral
  @LogAction
  @Override
  public List<ActionCommand> execute() throws UnsupportedOperationException {
    try {
      final var event = raiseAction.getEvent();

      final var commands = new ArrayList<ActionCommand>();

      final var extent = executionContext.scope().getExtent();
      final var eventHandler = executionContext.eventHandler();
      final var eventListener = executionContext.eventListener();

      final var evaluatedEvent = Event.ensureHasEvaluatedData(event, extent);

      // Dispatch the event
      if (evaluatedEvent.getChannel() == EventChannel.INTERNAL) {
        eventListener.onReceiveEvent(evaluatedEvent);
      } else {
        // Send the event through the event handler
        eventHandler.sendEvent(evaluatedEvent);
      }

      return commands;
    } catch (Exception e) {
      throw new UnsupportedOperationException("Could not execute raise action", e);
    }
  }
}
