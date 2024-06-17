package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.event.EventListener;
import at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachineEventHandler;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.tracing.Counters;
import at.ac.uibk.dps.cirrina.tracing.Gauges;
import jakarta.annotation.Nullable;

public record ExecutionContext(
    Scope scope,
    @Nullable Event raisingEvent,
    ServiceImplementationSelector serviceImplementationSelector,
    StateMachineEventHandler eventHandler,
    EventListener eventListener,
    Gauges gauges,
    Counters counters,
    boolean isWhile
) {

  public ExecutionContext withScope(Scope scope) {
    return new ExecutionContext(scope, raisingEvent, serviceImplementationSelector, eventHandler,
        eventListener, gauges, counters, isWhile);
  }

  public ExecutionContext withIsWhile(boolean isWhile) {
    return new ExecutionContext(scope, raisingEvent, serviceImplementationSelector, eventHandler,
        eventListener, gauges, counters, isWhile);
  }
}
