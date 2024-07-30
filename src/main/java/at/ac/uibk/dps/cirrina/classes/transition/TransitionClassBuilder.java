package at.ac.uibk.dps.cirrina.classes.transition;

import at.ac.uibk.dps.cirrina.classes.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.classes.helper.GuardResolver;
import at.ac.uibk.dps.cirrina.classes.statemachine.StateMachineClassBuilder;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.ActionOrActionReferenceDescription;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.GuardOrGuardReferenceDescription;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.OnTransitionDescription;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.TransitionDescription;
import at.ac.uibk.dps.cirrina.execution.object.action.Action;
import at.ac.uibk.dps.cirrina.execution.object.guard.Guard;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * @throws IllegalArgumentException If a guard name does not exist.
     * @throws IllegalArgumentException If an action name does not exist.
     */
    @Override
    public TransitionClass build() throws IllegalArgumentException {
      // Resolve guards
      Function<List<? extends GuardOrGuardReferenceDescription>, List<Guard>> resolveGuards = (List<? extends GuardOrGuardReferenceDescription> guards) ->
          guards.stream()
              .map(guardOrReferenceClass -> {
                var resolvedGuard = guardResolver.resolve(guardOrReferenceClass);
                if (resolvedGuard.isEmpty()) {
                  throw new IllegalArgumentException(
                      "A guard with the name '%s' does not exist".formatted(guardOrReferenceClass.toString()));
                }
                return resolvedGuard.get();
              })
              .toList();

      // Resolve actions
      Function<List<? extends ActionOrActionReferenceDescription>, List<Action>> resolveActions = (List<? extends ActionOrActionReferenceDescription> actions) ->
          actions.stream()
              .map(actionOrActionClass -> {
                var resolvedAction = actionResolver.tryResolve(actionOrActionClass);
                if (resolvedAction.isEmpty()) {
                  throw new IllegalArgumentException(
                      "An action with the name '%s' does not exist".formatted(actionOrActionClass.toString()));
                }
                return resolvedAction.get();
              })
              .toList();

      // Create the appropriate transitionClass
      switch (transitionDescription) {
        case OnTransitionDescription onTransitionClass -> {
          return new OnTransitionClass(
              onTransitionClass.getTarget(),
              transitionDescription.getElse(),
              resolveGuards.apply(onTransitionClass.getGuards()),
              resolveActions.apply(onTransitionClass.getActions()),
              onTransitionClass.getEvent()
          );
        }
        default -> {
          return new TransitionClass(
              transitionDescription.getTarget(),
              transitionDescription.getElse(),
              resolveGuards.apply(transitionDescription.getGuards()),
              resolveActions.apply(transitionDescription.getActions())
          );
        }
      }
    }
  }
}
