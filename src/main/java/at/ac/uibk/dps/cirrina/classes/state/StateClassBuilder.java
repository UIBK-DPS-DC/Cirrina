package at.ac.uibk.dps.cirrina.classes.state;

import at.ac.uibk.dps.cirrina.classes.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.ActionOrActionReferenceDescription;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.StateDescription;
import at.ac.uibk.dps.cirrina.execution.object.action.Action;
import at.ac.uibk.dps.cirrina.execution.object.action.TimeoutAction;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract stateClass builder
 */
public abstract class StateClassBuilder {

  /**
   * Construct a state class builder from a state description without a base state class.
   *
   * @param parentStateMachineId This must be removed.
   * @param stateDescription     State description.
   * @param actionResolver       Action resolver.
   * @return State class builder.
   */
  public static StateClassBuilder from(
      UUID parentStateMachineId,
      StateDescription stateDescription,
      ActionResolver actionResolver
  ) {
    return new StateClassFromDescriptionBuilder(parentStateMachineId, stateDescription, actionResolver);
  }

  /**
   * Construct a state class builder from a state description with a base state class.
   *
   * @param parentStateMachineId This must be removed.
   * @param stateDescription     State description.
   * @param actionResolver       Action resolver.
   * @param baseStateClass       Base state class.
   * @return State class builder.
   */
  public static StateClassBuilder from(
      UUID parentStateMachineId,
      StateDescription stateDescription,
      ActionResolver actionResolver,
      StateClass baseStateClass
  ) {
    return new StateClassFromDescriptionBuilder(parentStateMachineId, stateDescription, actionResolver, baseStateClass);
  }

  /**
   * Builds the stateClass.
   *
   * @return the stateClass.
   * @throws IllegalArgumentException In case the stateClass could not be built.
   */
  public abstract StateClass build() throws IllegalArgumentException;

  /**
   * StateClass builder implementation. Builds a stateClass based on a stateClass class.
   */
  private static final class StateClassFromDescriptionBuilder extends StateClassBuilder {

    /**
     * ID of the parent state machine class.
     */
    private final UUID parentStateMachineId;

    /**
     * State description.
     */
    private final StateDescription stateDescription;

    /**
     * Action resolver.
     */
    private final ActionResolver actionResolver;

    /**
     * Base state class, can be null in case a base class is being built.
     */
    private final @Nullable StateClass baseStateClass;

    /**
     * Initializes this builder instance for a base state class.
     *
     * @param parentStateMachineId ID of the parent state machine class.
     * @param stateDescription     State description.
     * @param actionResolver       Action resolver.
     */
    private StateClassFromDescriptionBuilder(
        UUID parentStateMachineId,
        StateDescription stateDescription,
        ActionResolver actionResolver
    ) {
      this.parentStateMachineId = parentStateMachineId;
      this.stateDescription = stateDescription;
      this.actionResolver = actionResolver;
      this.baseStateClass = null;
    }

    /**
     * Initializes this builder instance for a child state class.
     *
     * @param parentStateMachineId ID of the parent state machine class.
     * @param stateDescription     State description.
     * @param actionResolver       Action resolver.
     * @param baseStateClass       Base state class.
     */
    private StateClassFromDescriptionBuilder(
        UUID parentStateMachineId,
        StateDescription stateDescription,
        ActionResolver actionResolver,
        StateClass baseStateClass
    ) {
      this.parentStateMachineId = parentStateMachineId;
      this.stateDescription = stateDescription;
      this.actionResolver = actionResolver;
      this.baseStateClass = baseStateClass;
    }

    /**
     * Builds the state class.
     *
     * @return State class.
     * @throws IllegalArgumentException If an action name does not exist.
     * @throws IllegalArgumentException If an after action is not a timeout action.
     */
    @Override
    public StateClass build() throws IllegalArgumentException {
      // Resolve actions
      final Function<List<? extends ActionOrActionReferenceDescription>, List<Action>> resolveActions = (List<? extends ActionOrActionReferenceDescription> actions) ->
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

      final var entryActions = resolveActions.apply(stateDescription.getEntry());
      final var exitActions = resolveActions.apply(stateDescription.getExit());
      final var whileActions = resolveActions.apply(stateDescription.getWhile());
      final var afterActions = resolveActions.apply(stateDescription.getAfter());

      if (!afterActions.stream().allMatch(TimeoutAction.class::isInstance)) {
        throw new IllegalArgumentException("After action is not a timeout action");
      }

      // Create the state class
      if (baseStateClass == null) {
        return new StateClass(new StateClass.BaseParameters(
            parentStateMachineId,
            stateDescription.getName(),
            stateDescription.getLocalContext(),
            stateDescription.isInitial(),
            stateDescription.isTerminal(),
            entryActions,
            exitActions,
            whileActions,
            afterActions
        ));
      } else {
        return new StateClass(new StateClass.ChildParameters(
            parentStateMachineId,
            stateDescription.isInitial(),
            stateDescription.isTerminal(),
            entryActions,
            exitActions,
            whileActions,
            afterActions,
            baseStateClass
        ));
      }
    }
  }
}
