package at.ac.uibk.dps.cirrina.core.object.transition;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.ACTION_NAME_DOES_NOT_EXIST;
import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.GUARD_NAME_DOES_NOT_EXIST;

import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.lang.classes.helper.ActionOrActionReferenceClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.helper.GuardOrGuardReferenceClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.transition.OnTransitionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.transition.TransitionClass;
import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.guard.Guard;
import at.ac.uibk.dps.cirrina.core.object.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.core.object.helper.GuardResolver;
import at.ac.uibk.dps.cirrina.core.object.statemachine.ChildStateMachineBuilder;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachineBuilder;
import java.util.List;
import java.util.function.Function;

/**
 * Abstract transition builder.
 */
public abstract class TransitionBuilder {

  public static TransitionBuilder from(TransitionClass transitionClass, GuardResolver guardResolver, ActionResolver actionResolver) {
    return new TransitionFromClassBuilder(transitionClass, guardResolver, actionResolver);
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
    private final GuardResolver guardResolver;
    private final ActionResolver actionResolver;

    private TransitionFromClassBuilder(TransitionClass transitionClass, GuardResolver guardResolver, ActionResolver actionResolver) {
      this.transitionClass = transitionClass;
      this.guardResolver = guardResolver;
      this.actionResolver = actionResolver;
    }

    @Override
    public Transition build() throws IllegalArgumentException {
      // Resolve guards
      Function<List<GuardOrGuardReferenceClass>, List<Guard>> resolveGuards = (List<GuardOrGuardReferenceClass> guards) ->
          guards.stream()
              .map(guardOrReferenceClass -> {
                var resolvedGuard = guardResolver.resolve(guardOrReferenceClass);
                if (resolvedGuard.isEmpty()) {
                  throw new IllegalArgumentException(
                      VerificationException.from(GUARD_NAME_DOES_NOT_EXIST));
                }
                return resolvedGuard.get();
              })
              .toList();

      // Resolve actions
      Function<List<ActionOrActionReferenceClass>, List<Action>> resolveActions = (List<ActionOrActionReferenceClass> actions) ->
          actions.stream()
              .map(actionOrActionClass -> {
                var resolvedAction = actionResolver.resolve(actionOrActionClass);
                if (resolvedAction.isEmpty()) {
                  throw new IllegalArgumentException(
                      VerificationException.from(ACTION_NAME_DOES_NOT_EXIST));
                }
                return resolvedAction.get();
              })
              .toList();

      // Create the appropriate transition
      switch (transitionClass) {
        case OnTransitionClass onTransitionClass -> {
          return new OnTransition(
              onTransitionClass.target,
              transitionClass.elsee,
              resolveGuards.apply(onTransitionClass.guards),
              resolveActions.apply(onTransitionClass.actions),
              onTransitionClass.event
          );
        }
        default -> {
          return new Transition(
              transitionClass.target,
              transitionClass.elsee,
              resolveGuards.apply(transitionClass.guards),
              resolveActions.apply(transitionClass.actions)
          );
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
      if (transition instanceof OnTransition onTransition) {
        return new OnTransition(
            onTransition.getTarget(),
            onTransition.getElse(),
            onTransition.getGuards(),
            onTransition.getActionGraph().getActions(),
            onTransition.getEventName()
        );
      } else {
        return new Transition(
            transition.getTarget(),
            transition.getElse(),
            transition.getGuards(),
            transition.getActionGraph().getActions()
        );
      }
    }
  }
}
