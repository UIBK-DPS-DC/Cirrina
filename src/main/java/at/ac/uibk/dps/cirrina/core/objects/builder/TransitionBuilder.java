package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.core.objects.transitions.OnTransition;
import at.ac.uibk.dps.cirrina.core.objects.transitions.Transition;
import at.ac.uibk.dps.cirrina.lang.parser.classes.OnTransitionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.TransitionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.ActionOrActionReferenceClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TransitionBuilder {

  private final TransitionClass transitionClass;

  private final ActionResolver actionResolver;

  public TransitionBuilder(TransitionClass transitionClass, ActionResolver actionResolver) {
    this.transitionClass = transitionClass;
    this.actionResolver = actionResolver;
  }

  public Transition build()
      throws IllegalArgumentException {
    // Resolve actions
    Function<Optional<List<ActionOrActionReferenceClass>>, List<Action>> resolveActions = (Optional<List<ActionOrActionReferenceClass>> actions) ->
        actions.orElse(new ArrayList<ActionOrActionReferenceClass>()).stream()
            .map(actionResolver::resolve)
            .toList();

    // Create the appropriate transition
    switch (transitionClass) {
      case OnTransitionClass onTransitionClass -> {
        return new OnTransition(onTransitionClass.target, resolveActions.apply(onTransitionClass.actions),
            onTransitionClass.event);
      }
      default -> {
        return new Transition(transitionClass.target, resolveActions.apply(transitionClass.actions));
      }
    }
  }
}
