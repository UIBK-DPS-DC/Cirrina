package at.ac.uibk.dps.cirrina.object.transition;

import at.ac.uibk.dps.cirrina.lang.classes.action.ActionOrActionReferenceClass;
import at.ac.uibk.dps.cirrina.lang.classes.transition.OnTransitionClass;
import at.ac.uibk.dps.cirrina.lang.classes.transition.TransitionClass;
import at.ac.uibk.dps.cirrina.object.action.Action;
import at.ac.uibk.dps.cirrina.object.helper.ActionResolver;
import java.util.List;
import java.util.function.Function;

public abstract class TransitionBuilder {

  public static TransitionBuilder from(TransitionClass transitionClass, ActionResolver actionResolver) {
    return new TransitionFromClassBuilder(transitionClass, actionResolver);
  }

  public static TransitionBuilder from(Transition transition) {
    return new TransitionFromTransitionBuilder(transition);
  }

  public abstract Transition build() throws IllegalArgumentException;

  private static final class TransitionFromClassBuilder extends TransitionBuilder {

    private final TransitionClass transitionClass;
    private final ActionResolver actionResolver;

    private TransitionFromClassBuilder(TransitionClass transitionClass, ActionResolver actionResolver) {
      this.transitionClass = transitionClass;
      this.actionResolver = actionResolver;
    }

    @Override
    public Transition build() throws IllegalArgumentException {
      // Resolve actions
      Function<List<ActionOrActionReferenceClass>, List<Action>> resolveActions = (List<ActionOrActionReferenceClass> actions) ->
          actions.stream()
              .map(actionResolver::resolve)
              .toList();

      // Create the appropriate transition
      switch (transitionClass) {
        case OnTransitionClass onTransitionClass -> {
          return new OnTransition(onTransitionClass.target, transitionClass.elsee, resolveActions.apply(onTransitionClass.actions),
              onTransitionClass.event);
        }
        default -> {
          return new Transition(transitionClass.target, transitionClass.elsee, resolveActions.apply(transitionClass.actions));
        }
      }
    }
  }

  private static final class TransitionFromTransitionBuilder extends TransitionBuilder {

    private final Transition transition;

    private TransitionFromTransitionBuilder(Transition transition) {
      this.transition = transition;
    }

    @Override
    public Transition build() {
      return transition instanceof OnTransition onTransition
          ? new OnTransition(onTransition.target, onTransition.elsee, onTransition.allActions(), onTransition.eventName)
          : new Transition(transition.target, transition.elsee, transition.allActions());
    }
  }
}
