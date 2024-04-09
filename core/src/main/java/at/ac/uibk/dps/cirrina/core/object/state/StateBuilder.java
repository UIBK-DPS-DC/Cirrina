package at.ac.uibk.dps.cirrina.core.object.state;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.ACTION_NAME_DOES_NOT_EXIST;

import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.lang.classes.StateClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.helper.ActionOrActionReferenceClass;
import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.helper.ActionResolver;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class StateBuilder {

  private final StateClass stateClass;

  private final ActionResolver actionResolver;

  private final Optional<State> baseState;

  private StateBuilder(StateClass stateClass, ActionResolver actionResolver, Optional<State> baseState) {
    this.stateClass = stateClass;
    this.actionResolver = actionResolver;
    this.baseState = baseState;
  }

  public static StateBuilder from(StateClass stateClass, ActionResolver actionResolver, Optional<State> baseState) {
    return new StateBuilder(stateClass, actionResolver, baseState);
  }

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
        stateClass.name,
        stateClass.isInitial,
        stateClass.isTerminal,
        entryActions,
        exitActions,
        whileActions,
        stateClass.isAbstract,
        stateClass.isVirtual,
        baseState
    );

    // Create this state
    return new State(parameters);
  }
}
