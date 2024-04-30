package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.execution.instance.state.StateInstance;
import java.util.ArrayList;

public final class TransitionInitialCommand extends Command {

  private final StateInstance targetStateInstance;

  TransitionInitialCommand(ExecutionContext executionContext, StateInstance targetStateInstance) {
    super(executionContext);

    this.targetStateInstance = targetStateInstance;
  }

  @Override
  public void execute() throws CirrinaException {
    // New commands
    final var commands = new ArrayList<Command>();
    final var commandFactory = new CommandFactory(executionContext);

    // Change the active state
    commands.add(commandFactory.createStateChangeCommand(targetStateInstance));

    // Enter the target state
    commands.add(commandFactory.createStateEnterCommand(targetStateInstance));

    // Add commands to the front of the queue, replacing this executed command
    final var commandQueue = executionContext.commandQueueAdapter();

    commandQueue.addCommandsToFront(commands);
  }
}
