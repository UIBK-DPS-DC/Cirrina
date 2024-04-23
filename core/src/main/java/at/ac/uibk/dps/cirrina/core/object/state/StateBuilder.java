package at.ac.uibk.dps.cirrina.core.object.state;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.ACTION_NAME_DOES_NOT_EXIST;

import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.lang.classes.StateClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.helper.ActionOrActionReferenceClass;
import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.ActionGraph;
import at.ac.uibk.dps.cirrina.core.object.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachineBuilder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Abstract state builder
 */
public abstract class StateBuilder {

  public static StateBuilder from(UUID parentStateMachineId, StateClass stateClass, ActionResolver actionResolver,
      Optional<State> baseState) {
    return new StateFromClassBuilder(parentStateMachineId, stateClass, actionResolver, baseState);
  }

  public static StateBuilder from(UUID parentStateMachineId, State state) {
    return new StateFromObjectBuilder(parentStateMachineId, state);
  }

  /**
   * Builds the state.
   *
   * @return the state.
   * @throws IllegalArgumentException In case the state could not be built.
   */
  public abstract State build() throws IllegalArgumentException;

  /**
   * State builder implementation. Builds a state based on a state class.
   */
  private static final class StateFromClassBuilder extends StateBuilder {
    private final UUID parentStateMachineId;

    private final StateClass stateClass;

    private final ActionResolver actionResolver;

    private final Optional<State> baseState;

    private StateFromClassBuilder(UUID parentStateMachineId, StateClass stateClass, ActionResolver actionResolver, Optional<State> baseState) {
      this.parentStateMachineId = parentStateMachineId;
      this.stateClass = stateClass;
      this.actionResolver = actionResolver;
      this.baseState = baseState;
    }

    @Override
    public State build() throws IllegalArgumentException {
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

      var entryActions = resolveActions.apply(stateClass.entry);
      var exitActions = resolveActions.apply(stateClass.exit);
      var whileActions = resolveActions.apply(stateClass.whilee);

      var parameters = new State.Parameters(
          parentStateMachineId,
          stateClass.name,
          stateClass.localContext,
          stateClass.initial,
          stateClass.terminal,
          entryActions,
          exitActions,
          whileActions,
          stateClass.abstractt,
          stateClass.virtual,
          baseState
      );

      // Create this state
      return new State(parameters);
    }
  }

  /**
   * State builder implementation. Builds a state based on another state.
   */
  private static final class StateFromObjectBuilder extends StateBuilder {

    private final UUID parentStateMachineId;

    private final State state;

    public StateFromObjectBuilder(UUID parentStateMachineId, State state) {
      this.parentStateMachineId = parentStateMachineId;
      this.state = state;
    }

    @Override
    public State build() throws IllegalArgumentException {

      var parameters = new State.Parameters(
          parentStateMachineId,
          state.getName(),
          state.getLocalContextClass(),
          state.isInitial(),
          state.isTerminal(),
          state.getEntryActionGraph().getActions(),
          state.getExitActionGraph().getActions(),
          state.getWhileActionGraph().getActions(),
          state.isAbstract(),
          state.isVirtual(),
          Optional.empty()
      );

      // Create this state
      return new State(parameters);
    }
  }
}
