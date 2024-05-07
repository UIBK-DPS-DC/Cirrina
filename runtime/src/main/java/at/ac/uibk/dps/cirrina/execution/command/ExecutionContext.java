package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.object.event.EventListener;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstanceEventHandler;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;

public record ExecutionContext(
    Scope scope,
    ServiceImplementationSelector serviceImplementationSelector,
    StateMachineInstanceEventHandler eventHandler,
    EventListener eventListener,
    boolean isWhile
) {

  public ExecutionContext withScope(Scope scope) {
    return new ExecutionContext(scope, serviceImplementationSelector, eventHandler,
        eventListener, isWhile);
  }

  public ExecutionContext withIsWhile(boolean isWhile) {
    return new ExecutionContext(scope, serviceImplementationSelector, eventHandler,
        eventListener, isWhile);
  }
}
