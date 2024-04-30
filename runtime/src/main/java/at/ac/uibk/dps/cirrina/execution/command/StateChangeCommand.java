package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.execution.instance.state.StateInstance;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;

public final class StateChangeCommand extends Command {

  private final StateInstance targetStateInstance;

  StateChangeCommand(ExecutionContext executionContext, StateInstance targetStateInstance) {
    super(executionContext);

    this.targetStateInstance = targetStateInstance;
  }

  @Override
  public void execute() throws CirrinaException {
    // Require state machine scope
    final var stateMachineInstance = (StateMachineInstance) executionContext.scope();

    if (stateMachineInstance == null) {
      throw CirrinaException.from("Event scope must be a state machine instance");
    }

    // Update the active state
    stateMachineInstance.updateActiveState(targetStateInstance);
  }
}
