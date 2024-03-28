package at.ac.uibk.dps.cirrina.core.object.transition;

import at.ac.uibk.dps.cirrina.core.lang.classes.action.ActionOrActionReferenceClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.transition.OnTransitionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.transition.TransitionClass;
import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.core.object.statemachine.ChildStateMachineBuilder;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachineBuilder;
import java.util.List;
import java.util.function.Function;

/**
 * Abstract transition builder.
 */
public abstract class TransitionBuilder {

  public static TransitionBuilder from(TransitionClass transitionClass, ActionResolver actionResolver) {
    return new TransitionFromClassBuilder(transitionClass, actionResolver);
  }

  public static TransitionBuilder from(Transition transition) {
    return new TransitionFromTransitionBuilder(transition);
  }

  /**
   * Builds the transition.
   *
   * @return the transition.
   * @throws IllegalArgumentException In case the transition could not be built.
   */
  public abstract Transition build() throws IllegalArgumentException;

  /**
   * Transition builder implementation. Builds a transition based on a transition class.
   *
   * @see StateMachineBuilder
   */
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

  /**
   * Transition builder implementation. Builds a transition based on another transition.
   *
   * @see ChildStateMachineBuilder
   */
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
