package at.ac.uibk.dps.cirrina.core.object.transition;

import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.lang.classes.action.ActionOrActionReferenceClass;
import at.ac.uibk.dps.cirrina.lang.classes.transition.OnTransitionClass;
import at.ac.uibk.dps.cirrina.lang.classes.transition.TransitionClass;
import java.util.List;
import java.util.function.Function;

public final class TransitionBuilder {

  private final TransitionClass transitionClass;

  private final ActionResolver actionResolver;

  private TransitionBuilder(TransitionClass transitionClass, ActionResolver actionResolver) {
    this.transitionClass = transitionClass;
    this.actionResolver = actionResolver;
  }

  public static TransitionBuilder from(TransitionClass transitionClass, ActionResolver actionResolver) {
    return new TransitionBuilder(transitionClass, actionResolver);
  }

  public Transition build()
      throws IllegalArgumentException {
    // Resolve actions
    Function<List<ActionOrActionReferenceClass>, List<Action>> resolveActions = (List<ActionOrActionReferenceClass> actions) ->
        actions.stream()
            .map(actionResolver::resolve)
            .toList();

    // Create the appropriate transition
    switch (transitionClass) {
      case OnTransitionClass onTransitionClass -> {
        return new OnTransition(onTransitionClass.target, resolveActions.apply(onTransitionClass.actions), onTransitionClass.event);
      }
      default -> {
        return new Transition(transitionClass.target, resolveActions.apply(transitionClass.actions));
      }
    }
  }
}