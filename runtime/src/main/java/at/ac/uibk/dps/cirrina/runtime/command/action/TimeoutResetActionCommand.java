package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

/**
 * Timeout reset action command, executes a timeout reset action.
 */
public final class TimeoutResetActionCommand extends ActionCommand {

  /**
   * The timeout reset action object.
   */
  private final TimeoutResetAction timeoutResetAction;

  /**
   * Initializes this timeout reset action command object.
   *
   * @param scope              Scope of execution, unused.
   * @param timeoutResetAction Timeout reset action object.
   * @param isWhile            Whether this action is executed while in a state, unused.
   */
  public TimeoutResetActionCommand(Scope scope, TimeoutResetAction timeoutResetAction, boolean isWhile) {
    super(scope, isWhile);

    this.timeoutResetAction = timeoutResetAction;
  }

  /**
   * Execute this timeout reset action command.
   * <p>
   * Will create no new commands.
   * <p>
   * Will stop the timeout action with the name provided by the timeout reset action as a side effect.
   *
   * @param executionContext Execution context.
   * @return No new commands.
   * @throws CirrinaException In case of an error.
   */
  @Override
  public List<Command> execute(ExecutionContext executionContext) throws CirrinaException {
    final var stateMachineInstance = executionContext.stateMachineInstance();

    // Attempt to stop the timeout action
    stateMachineInstance.stopTimeoutAction(timeoutResetAction.getAction());

    return List.of();
  }
}
