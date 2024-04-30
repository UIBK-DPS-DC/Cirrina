package at.ac.uibk.dps.cirrina.execution.instance.statemachine;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;

public class StateMachineInstanceEventHandler {

  private final StateMachineInstance stateMachineInstance;

  private final EventHandler eventHandler;

  public StateMachineInstanceEventHandler(StateMachineInstance stateMachineInstance, EventHandler eventHandler) {
    this.stateMachineInstance = stateMachineInstance;
    this.eventHandler = eventHandler;
  }

  public void sendEvent(Event event) throws CirrinaException {
    eventHandler.sendEvent(event, stateMachineInstance.getStateMachineInstanceId().toString());
  }
}
