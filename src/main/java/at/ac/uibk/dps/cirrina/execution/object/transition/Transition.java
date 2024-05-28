package at.ac.uibk.dps.cirrina.execution.object.transition;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_TRANSITION_TARGET_STATE_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_TRANSITION_TYPE;

import at.ac.uibk.dps.cirrina.classes.transition.TransitionClass;
import at.ac.uibk.dps.cirrina.execution.command.ActionCommand;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class Transition {

  private final TransitionClass transitionClass;

  private final boolean isElse;

  public Transition(TransitionClass transitionClass, boolean isElse) {
    this.transitionClass = transitionClass;
    this.isElse = isElse;

    assert !isElse || transitionClass.getElse().isPresent();
  }

  public boolean isInternalTransition() {
    return transitionClass.getTargetStateName().isEmpty();
  }

  public TransitionClass getTransitionObject() {
    return transitionClass;
  }

  public Optional<String> getTargetStateName() {
    return isElse ? transitionClass.getElse() : transitionClass.getTargetStateName();
  }

  public List<ActionCommand> getActionCommands(CommandFactory commandFactory) {
    List<ActionCommand> actionCommands = new ArrayList<>();

    new TopologicalOrderIterator<>(transitionClass.getActionGraph()).forEachRemaining(
        action -> actionCommands.add(commandFactory.createActionCommand(action)));

    return actionCommands;
  }

  public boolean isElse() {
    return isElse;
  }

  public Attributes getAttributes() {
    return Attributes.of(
        AttributeKey.stringKey(ATTR_TRANSITION_TARGET_STATE_NAME), getTargetStateName().orElse(""),
        AttributeKey.stringKey(ATTR_TRANSITION_TYPE), isInternalTransition() ? "internal" : "external"
    );
  }
}
