package ac.at.uibk.dps.nexa.core.objects.builder;

import ac.at.uibk.dps.nexa.core.objects.actions.Action;
import ac.at.uibk.dps.nexa.core.objects.helper.ActionResolver;
import ac.at.uibk.dps.nexa.core.objects.transitions.OnTransition;
import ac.at.uibk.dps.nexa.core.objects.transitions.Transition;
import ac.at.uibk.dps.nexa.lang.parser.classes.OnTransitionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.TransitionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.ActionOrActionReferenceClass;
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
    Function<Optional<List<ActionOrActionReferenceClass>>, List<Action>> resolveActions = (Optional<List<ActionOrActionReferenceClass>> actions) ->
        actions.orElse(new ArrayList<ActionOrActionReferenceClass>()).stream()
            .map(actionResolver::resolve)
            .toList();

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
