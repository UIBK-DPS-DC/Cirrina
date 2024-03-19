package at.ac.uibk.dps.cirrina.runtime.command;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.runtime.StateMachineInstance;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class StateEntryCommand extends Command {

  private static final Logger logger = LogManager.getLogger();

  private State state;

  public StateEntryCommand(StateMachineInstance stateMachineInstance, State state) {
    super(stateMachineInstance);

    this.state = state;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    logger.debug("Instance-{}: Execute state entry command", stateMachineInstance.instanceId);

    var commands = new ArrayList<Command>();

    // Add the state change command that performs the actual state change
    commands.add(new StateChangeCommand(stateMachineInstance, state));

    // Then add the entry actions, executed in topological order:
    // Append the entry actions to the command list
    new TopologicalOrderIterator<>(state.getEntry()).forEachRemaining(
        action -> commands.add(new ActionCommand(stateMachineInstance, action, false)));

    // Append the while actions to the command list
    new TopologicalOrderIterator<>(state.getWhile()).forEachRemaining(
        action -> commands.add(new ActionCommand(stateMachineInstance, action, true)));

    // TODO: Start timeout actions

    return commands;
  }
}
