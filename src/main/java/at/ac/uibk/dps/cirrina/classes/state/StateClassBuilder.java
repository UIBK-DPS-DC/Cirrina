package at.ac.uibk.dps.cirrina.classes.state;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.ACTION_NAME_DOES_NOT_EXIST;
import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.AFTER_ACTION_IS_NOT_A_TIMEOUT_ACTION;

import at.ac.uibk.dps.cirrina.classes.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.csml.description.StateDescription;
import at.ac.uibk.dps.cirrina.csml.description.helper.ActionOrActionReferenceDescription;
import at.ac.uibk.dps.cirrina.execution.object.action.Action;
import at.ac.uibk.dps.cirrina.execution.object.action.TimeoutAction;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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
   * Construct a state class builder from a state class.
   *
   * @param parentStateMachineId This must be removed.
   * @param stateClass           State class.
   * @return State class builder.
   */
  public static StateClassBuilder from(UUID parentStateMachineId, StateClass stateClass) {
    return new StateClassFromClassBuilder(parentStateMachineId, stateClass);
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
     * @throws IllegalArgumentException In case of error.
     */
    @Override
    public StateClass build() throws IllegalArgumentException {
      // Resolve actions
      final Function<List<ActionOrActionReferenceDescription>, List<Action>> resolveActions = (List<ActionOrActionReferenceDescription> actions) ->
          actions.stream()
              .map(actionOrActionClass -> {
                var resolvedAction = actionResolver.tryResolve(actionOrActionClass);
                if (resolvedAction.isEmpty()) {
                  throw new IllegalArgumentException(VerificationException.from(ACTION_NAME_DOES_NOT_EXIST));
                }
                return resolvedAction.get();
              })
              .toList();

      final var entryActions = resolveActions.apply(stateDescription.entry);
      final var exitActions = resolveActions.apply(stateDescription.exit);
      final var whileActions = resolveActions.apply(stateDescription.whilee);
      final var afterActions = resolveActions.apply(stateDescription.after);

      if (!afterActions.stream().allMatch(action -> action instanceof TimeoutAction)) {
        throw new IllegalArgumentException(VerificationException.from(AFTER_ACTION_IS_NOT_A_TIMEOUT_ACTION));
      }

      // Create the state class
      if (baseStateClass == null) {
        return new StateClass(new StateClass.BaseParameters(
            parentStateMachineId,
            stateDescription.name,
            stateDescription.localContext.orElse(null),
            stateDescription.initial,
            stateDescription.terminal,
            stateDescription.abstractt,
            stateDescription.virtual,
            entryActions,
            exitActions,
            whileActions,
            afterActions
        ));
      } else {
        return new StateClass(new StateClass.ChildParameters(
            parentStateMachineId,
            stateDescription.initial,
            stateDescription.terminal,
            stateDescription.abstractt,
            entryActions,
            exitActions,
            whileActions,
            afterActions,
            baseStateClass
        ));
      }
    }
  }

  /**
   * StateClass builder implementation. Builds a stateClass based on another stateClass.
   */
  private static final class StateClassFromClassBuilder extends StateClassBuilder {

    /**
     * ID of the parent state machine class.
     */
    private final UUID parentStateMachineId;

    /**
     * State class.
     */
    private final StateClass stateClass;

    /**
     * Initializes this builder from a state class.
     *
     * @param parentStateMachineId ID of the parent state machine class.
     * @param stateClass           State class.
     */
    public StateClassFromClassBuilder(UUID parentStateMachineId, StateClass stateClass) {
      this.parentStateMachineId = parentStateMachineId;
      this.stateClass = stateClass;
    }

    /**
     * Builds the state class.
     *
     * @return State class.
     * @throws IllegalArgumentException In case of error.
     */
    @Override
    public StateClass build() throws IllegalArgumentException {
      final var parameters = new StateClass.BaseParameters(
          parentStateMachineId,
          stateClass.getName(),
          stateClass.getLocalContextDescription().orElse(null),
          stateClass.isInitial(),
          stateClass.isTerminal(),
          stateClass.isAbstract(),
          stateClass.isVirtual(),
          stateClass.getEntryActionGraph().getActions(),
          stateClass.getExitActionGraph().getActions(),
          stateClass.getWhileActionGraph().getActions(),
          stateClass.getAfterActionGraph().getActions()
      );

      // Create this stateClass
      return new StateClass(parameters);
    }
  }
}
