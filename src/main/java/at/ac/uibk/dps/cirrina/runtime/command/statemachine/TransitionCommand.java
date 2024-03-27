package at.ac.uibk.dps.cirrina.runtime.command.statemachine;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.action.ActionCommand;
import at.ac.uibk.dps.cirrina.runtime.command.state.StateEntryCommand;
import at.ac.uibk.dps.cirrina.runtime.command.state.StateExitCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import at.ac.uibk.dps.cirrina.runtime.instance.TransitionInstance;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class TransitionCommand implements Command {

  private final TransitionInstance transition;

  private final StateInstance targetState;


  public TransitionCommand(TransitionInstance transition, StateInstance targetState) throws RuntimeException {
    this.transition = transition;
    this.targetState = targetState;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    final var commands = new ArrayList<Command>();

    final var stateMachineInstance = executionContext.stateMachineInstance();

    // Exit the currently active state
    commands.add(new StateExitCommand(executionContext.stateMachineInstance().getStatus().getActivateState()));

    // Append the entry actions to the command list
    new TopologicalOrderIterator<>(transition.getTransition().actions).forEachRemaining(
        action -> commands.add(ActionCommand.from(stateMachineInstance, action, false)));

    // Change the active state
    commands.add(new StateChangeCommand(targetState));

    // Enter the target state
    commands.add(new StateEntryCommand(targetState));

    return commands;
  }
}
