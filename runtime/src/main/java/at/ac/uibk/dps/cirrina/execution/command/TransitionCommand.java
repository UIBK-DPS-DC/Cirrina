package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.execution.instance.state.StateInstance;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import at.ac.uibk.dps.cirrina.execution.instance.transition.TransitionInstance;
import java.util.ArrayList;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class TransitionCommand extends Command {

  private final TransitionInstance transitionInstance;

  private final StateInstance targetStateInstance;

  private final boolean isElse;

  TransitionCommand(ExecutionContext executionContext, TransitionInstance transitionInstance, StateInstance targetStateInstance,
      boolean isElse) {
    super(executionContext);

    this.transitionInstance = transitionInstance;
    this.targetStateInstance = targetStateInstance;
    this.isElse = isElse;
  }

  @Override
  public void execute() throws CirrinaException {
    // Require state machine scope
    final var stateMachineInstance = (StateMachineInstance) executionContext.scope();

    if (stateMachineInstance == null) {
      throw CirrinaException.from("Event scope must be a state machine instance");
    }

    // New commands
    final var commands = new ArrayList<Command>();
    final var commandFactory = new CommandFactory(executionContext);

    final var status = executionContext.status();
    final var currentStateInstance = status.getActivateState();
    final var transitionObject = transitionInstance.getTransition();

    // Exit the currently active state
    commands.add(commandFactory.createStateExitCommand(currentStateInstance));

    if (!isElse) {
      // Append the entry actions to the command list
      new TopologicalOrderIterator<>(transitionObject.getActionGraph()).forEachRemaining(
          action -> commands.add(commandFactory.createActionCommand(action)));
    }

    // Change the active state
    commands.add(commandFactory.createStateChangeCommand(targetStateInstance));

    // Enter the target state
    commands.add(commandFactory.createStateEnterCommand(targetStateInstance));

    // Add commands to the front of the queue, replacing this executed command
    final var commandQueue = executionContext.commandQueueAdapter();

    commandQueue.addCommandsToFront(commands);
  }
}
