package at.ac.uibk.dps.cirrina.runtime.command.transition;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.action.ActionCommand;
import at.ac.uibk.dps.cirrina.runtime.command.state.StateEntryCommand;
import at.ac.uibk.dps.cirrina.runtime.command.state.StateExitCommand;
import at.ac.uibk.dps.cirrina.runtime.command.statemachine.StateChangeCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import at.ac.uibk.dps.cirrina.runtime.instance.TransitionInstance;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class TransitionCommand implements Command {

  private final TransitionInstance transition;

  private final StateInstance targetState;

  private final boolean isElse;

  public TransitionCommand(TransitionInstance transition, StateInstance targetState, boolean isElse) {
    this.transition = transition;
    this.targetState = targetState;
    this.isElse = isElse;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws CirrinaException {
    final var commands = new ArrayList<Command>();

    final var stateMachineInstance = executionContext.stateMachineInstance();

    // Exit the currently active state
    commands.add(new StateExitCommand(executionContext.stateMachineInstance().getStatus().getActivateState()));

    if (!isElse) {
      // Append the entry actions to the command list
      new TopologicalOrderIterator<>(transition.getTransition().getActionGraph()).forEachRemaining(
          action -> commands.add(ActionCommand.from(stateMachineInstance, action, false)));
    }

    // Change the active state
    commands.add(new StateChangeCommand(targetState));

    // Enter the target state
    commands.add(new StateEntryCommand(targetState));

    return commands;
  }
}
