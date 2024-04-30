package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.object.event.EventListener;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.CommandQueueAdapter;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstanceEventHandler;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.Status;

public record ExecutionContext(
    Scope scope,
    CommandQueueAdapter commandQueueAdapter,
    Status status,
    StateMachineInstanceEventHandler eventHandler,
    EventListener eventListener,
    boolean isWhile
) {

  public ExecutionContext withScope(Scope scope) {
    return new ExecutionContext(scope, commandQueueAdapter, status, eventHandler, eventListener, isWhile);
  }

  public ExecutionContext withIsWhile(boolean isWhile) {
    return new ExecutionContext(scope, commandQueueAdapter, status, eventHandler, eventListener, isWhile);
  }
}
