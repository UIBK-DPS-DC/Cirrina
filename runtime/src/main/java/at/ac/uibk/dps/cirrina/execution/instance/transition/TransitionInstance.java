package at.ac.uibk.dps.cirrina.execution.instance.transition;

import at.ac.uibk.dps.cirrina.core.object.transition.Transition;
import at.ac.uibk.dps.cirrina.execution.command.ActionCommand;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class TransitionInstance {

  private final Transition transitionObject;

  private final boolean isElse;

  public TransitionInstance(Transition transitionObject, boolean isElse) {
    this.transitionObject = transitionObject;
    this.isElse = isElse;

    assert !isElse || transitionObject.getElse().isPresent();
  }

  public boolean isInternalTransition() {
    return transitionObject.getTargetName().isEmpty();
  }

  public Transition getTransitionObject() {
    return transitionObject;
  }

  public String getTargetStateName() {
    return isElse ? transitionObject.getElse().get() : transitionObject.getTargetName();
  }

  public List<ActionCommand> getActionCommands(CommandFactory commandFactory) {
    List<ActionCommand> actionCommands = new ArrayList<>();

    new TopologicalOrderIterator<>(transitionObject.getActionGraph()).forEachRemaining(
        action -> actionCommands.add(commandFactory.createActionCommand(action)));

    return actionCommands;
  }

  public boolean isElse() {
    return isElse;
  }
}
