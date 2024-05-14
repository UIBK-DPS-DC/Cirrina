package at.ac.uibk.dps.cirrina.classes.transition;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.ACTION_NAME_DOES_NOT_EXIST;
import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.GUARD_NAME_DOES_NOT_EXIST;

import at.ac.uibk.dps.cirrina.classes.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.classes.helper.GuardResolver;
import at.ac.uibk.dps.cirrina.classes.statemachine.ChildStateMachineClassBuilder;
import at.ac.uibk.dps.cirrina.classes.statemachine.StateMachineClassBuilder;
import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.csml.description.helper.ActionOrActionReferenceDescription;
import at.ac.uibk.dps.cirrina.csml.description.helper.GuardOrGuardReferenceDescription;
import at.ac.uibk.dps.cirrina.csml.description.transition.OnTransitionDescription;
import at.ac.uibk.dps.cirrina.csml.description.transition.TransitionDescription;
import at.ac.uibk.dps.cirrina.execution.object.action.Action;
import at.ac.uibk.dps.cirrina.execution.object.guard.Guard;
import java.util.List;
import java.util.function.Function;

/**
 * Abstract transitionClass builder.
 */
public abstract class TransitionClassBuilder {

  /**
   * Construct a builder from a transition description.
   *
   * @param transitionDescription Transition description.
   * @param guardResolver         Guard resolver.
   * @param actionResolver        Action resolver.
   * @return Builder.
   */
  public static TransitionClassBuilder from(
      TransitionDescription transitionDescription,
      GuardResolver guardResolver,
      ActionResolver actionResolver
  ) {
    return new TransitionClassFromDescriptionBuilder(transitionDescription, guardResolver, actionResolver);
  }

  /**
   * Construct a builder from a transition class.
   *
   * @param transitionClass Transition class.
   * @return Builder.
   */
  public static TransitionClassBuilder from(TransitionClass transitionClass) {
    return new TransitionClassFromClassBuilder(transitionClass);
  }

  /**
   * Builds the transitionClass.
   *
   * @return the transitionClass.
   * @throws IllegalArgumentException In case the transitionClass could not be built.
   */
  public abstract TransitionClass build() throws IllegalArgumentException;

  /**
   * TransitionClass builder implementation. Builds a transitionClass based on a transitionClass class.
   *
   * @see StateMachineClassBuilder
   */
  private static final class TransitionClassFromDescriptionBuilder extends TransitionClassBuilder {

    /**
     * Transition description.
     */
    private final TransitionDescription transitionDescription;

    /**
     * Guard resolver.
     */
    private final GuardResolver guardResolver;

    /**
     * Action resolver.
     */
    private final ActionResolver actionResolver;

    /**
     * Initializes this builder.
     *
     * @param transitionDescription Transition description.
     * @param guardResolver         Guard resolver.
     * @param actionResolver        Action resolver.
     */
    private TransitionClassFromDescriptionBuilder(
        TransitionDescription transitionDescription,
        GuardResolver guardResolver,
        ActionResolver actionResolver
    ) {
      this.transitionDescription = transitionDescription;
      this.guardResolver = guardResolver;
      this.actionResolver = actionResolver;
    }

    /**
     * Builds the transition class.
     *
     * @return Transition class.
     * @throws IllegalArgumentException In case of error.
     */
    @Override
    public TransitionClass build() throws IllegalArgumentException {
      // Resolve guards
      Function<List<GuardOrGuardReferenceDescription>, List<Guard>> resolveGuards = (List<GuardOrGuardReferenceDescription> guards) ->
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
      Function<List<ActionOrActionReferenceDescription>, List<Action>> resolveActions = (List<ActionOrActionReferenceDescription> actions) ->
          actions.stream()
              .map(actionOrActionClass -> {
                var resolvedAction = actionResolver.tryResolve(actionOrActionClass);
                if (resolvedAction.isEmpty()) {
                  throw new IllegalArgumentException(
                      VerificationException.from(ACTION_NAME_DOES_NOT_EXIST));
                }
                return resolvedAction.get();
              })
              .toList();

      // Create the appropriate transitionClass
      switch (transitionDescription) {
        case OnTransitionDescription onTransitionClass -> {
          return new OnTransitionClass(
              onTransitionClass.target,
              transitionDescription.elsee.orElse(null),
              resolveGuards.apply(onTransitionClass.guards),
              resolveActions.apply(onTransitionClass.actions),
              onTransitionClass.event
          );
        }
        default -> {
          return new TransitionClass(
              transitionDescription.target,
              transitionDescription.elsee.orElse(null),
              resolveGuards.apply(transitionDescription.guards),
              resolveActions.apply(transitionDescription.actions)
          );
        }
      }
    }
  }

  /**
   * TransitionClass builder implementation. Builds a transitionClass based on another transitionClass.
   *
   * @see ChildStateMachineClassBuilder
   */
  private static final class TransitionClassFromClassBuilder extends TransitionClassBuilder {

    /**
     * Transition class.
     */
    private final TransitionClass transitionClass;

    /**
     * Initializes this builder.
     *
     * @param transitionClass Transition class.
     */
    private TransitionClassFromClassBuilder(TransitionClass transitionClass) {
      this.transitionClass = transitionClass;
    }

    /**
     * Builds the transition class.
     *
     * @return Transition class.
     * @throws IllegalArgumentException In case of error.
     */
    @Override
    public TransitionClass build() {
      if (transitionClass instanceof OnTransitionClass onTransition) {
        return new OnTransitionClass(
            onTransition.getTargetStateName(),
            onTransition.getElse().orElse(null),
            onTransition.getGuards(),
            onTransition.getActionGraph().getActions(),
            onTransition.getEventName()
        );
      } else {
        return new TransitionClass(
            transitionClass.getTargetStateName(),
            transitionClass.getElse().orElse(null),
            transitionClass.getGuards(),
            transitionClass.getActionGraph().getActions()
        );
      }
    }
  }
}
