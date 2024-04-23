package at.ac.uibk.dps.cirrina.runtime.command.action;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.lang.classes.event.EventChannel;
import at.ac.uibk.dps.cirrina.core.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

/**
 * Raise action command, provides a command that can execute a raise action.
 */
public final class RaiseActionCommand extends ActionCommand {

  /**
   * Raise action.
   */
  private final RaiseAction raiseAction;

  /**
   * Initializes this raise action command.
   *
   * @param scope       Execution scope.
   * @param raiseAction Action.
   * @param isWhile     Is a while action.
   */
  public RaiseActionCommand(Scope scope, RaiseAction raiseAction, boolean isWhile) {
    super(scope, isWhile);

    this.raiseAction = raiseAction;
  }

  /**
   * Executes this assign action command. After execution, the variable described in the assign action will have been written to, given the
   * value of the assign action value.
   * <p>
   * This command produces no new commands.
   *
   * @param executionContext Execution context.
   * @return Empty list.
   * @throws CirrinaException In case the command could not be executed.
   */
  @Override
  public List<Command> execute(ExecutionContext executionContext) throws CirrinaException {
    // Acquire the extent
    final var extent = scope.getExtent();

    // Acquire the event handler
    final var eventHandler = executionContext.eventHandler();

    // Acquire the source
    final var source = executionContext.stateMachineInstance().getInstanceId().toString();

    // Get the event, we make sure that it has been evaluated to avoid sending expressions instead of the expression-evaluated data
    final var event = Event.ensureHasEvaluatedData(raiseAction.getEvent(), extent);

    if (event.getChannel() == EventChannel.INTERNAL) {
      executionContext.stateMachineInstance().onReceiveEvent(event);
    } else {
      try {
        // Send the event through the event handler
        eventHandler.sendEvent(event, source);
      } catch (CirrinaException e) {
        throw CirrinaException.from("Could not execute raise action command: %s", e.getMessage());
      }
    }

    return List.of();
  }
}
