package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;

public final class ActionTimeoutResetCommand extends Command {

  private final TimeoutResetAction timeoutResetAction;

  ActionTimeoutResetCommand(ExecutionContext executionContext, TimeoutResetAction timeoutResetAction) {
    super(executionContext);

    this.timeoutResetAction = timeoutResetAction;
  }

  @Override
  public void execute() throws CirrinaException {
    // Require state machine scope
    final var stateMachineInstance = (StateMachineInstance) executionContext.scope();

    if (stateMachineInstance == null) {
      throw CirrinaException.from("Event scope must be a state machine instance");
    }

    // Attempt to stop the timeout action
    stateMachineInstance.stopTimeoutAction(timeoutResetAction.getAction());
  }
}
