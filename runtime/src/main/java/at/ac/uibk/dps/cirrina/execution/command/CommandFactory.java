package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.core.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.core.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.core.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.core.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.instance.state.StateInstance;
import at.ac.uibk.dps.cirrina.execution.instance.transition.TransitionInstance;

public class CommandFactory {

  private ExecutionContext executionContext;

  public CommandFactory(ExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  public Command createActionCommand(Action action) {
    switch (action) {
      case AssignAction assignAction -> {
        return new ActionAssignCommand(executionContext, assignAction);
      }
      case CreateAction createAction -> {
        return new ActionCreateCommand(executionContext, createAction);
      }
      case InvokeAction invokeAction -> {
        return new ActionInvokeCommand(executionContext, invokeAction);
      }
      case MatchAction matchAction -> {
        return new ActionMatchCommand(executionContext, matchAction);
      }
      case RaiseAction raiseAction -> {
        return new ActionRaiseCommand(executionContext, raiseAction);
      }
      case TimeoutAction timeoutAction -> {
        return new ActionTimeoutCommand(executionContext, timeoutAction);
      }
      case TimeoutResetAction timeoutResetAction -> {
        return new ActionTimeoutResetCommand(executionContext, timeoutResetAction);
      }
      default -> throw new IllegalArgumentException("Unexpected action");
    }
  }

  public Command createEventCommand(Event event) {
    return new EventCommand(executionContext, event);
  }

  public Command createStateChangeCommand(StateInstance targetStateInstance) {
    return new StateChangeCommand(executionContext, targetStateInstance);
  }

  public Command createStateEnterCommand(StateInstance enteringStateInstance) {
    return new StateEnterCommand(executionContext, enteringStateInstance);
  }

  public Command createStateExitCommand(StateInstance exitingStateInstance) {
    return new StateExitCommand(executionContext, exitingStateInstance);
  }

  public Command createTransitionInitialCommand(StateInstance targetStateInstance) {
    return new TransitionInitialCommand(executionContext, targetStateInstance);
  }

  public Command createTransitionCommand(TransitionInstance transitionInstance, StateInstance targetStateInstance, boolean isElse) {
    return new TransitionCommand(executionContext, transitionInstance, targetStateInstance, isElse);
  }
}
