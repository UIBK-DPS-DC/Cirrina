package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.execution.instance.state.StateInstance;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import java.util.ArrayList;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class StateExitCommand extends Command {

  private final StateInstance exitingStateInstance;

  StateExitCommand(ExecutionContext executionContext, StateInstance exitingStateInstance) {
    super(executionContext);

    this.exitingStateInstance = exitingStateInstance;
  }

  @Override
  public void execute() throws CirrinaException {
    // Require state machine scope
    final var stateMachineInstance = (StateMachineInstance) executionContext.scope();

    if (stateMachineInstance == null) {
      throw CirrinaException.from("Event scope must be a state machine instance");
    }

    // Create new execution context with state scope
    final var stateScopeExecutionContext = executionContext.withScope(exitingStateInstance);

    // New commands
    final var commands = new ArrayList<Command>();
    final var commandFactory = new CommandFactory(stateScopeExecutionContext);

    // Append the entry actions to the command list
    new TopologicalOrderIterator<>(exitingStateInstance.getState().getExitActionGraph()).forEachRemaining(
        action -> commands.add(commandFactory.createActionCommand(action)));

    // Stop all current timeout actions
    stateMachineInstance.stopAllTimeoutActions();

    // Add commands to the front of the queue, replacing this executed command
    final var commandQueue = executionContext.commandQueueAdapter();

    commandQueue.addCommandsToFront(commands);
  }
}
