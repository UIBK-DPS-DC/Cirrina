package at.ac.uibk.dps.cirrina.object.state;

import at.ac.uibk.dps.cirrina.lang.classes.StateClass;
import at.ac.uibk.dps.cirrina.lang.classes.action.ActionOrActionReferenceClass;
import at.ac.uibk.dps.cirrina.object.action.Action;
import at.ac.uibk.dps.cirrina.object.helper.ActionResolver;
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
            .map(actionResolver::resolve)
            .toList();

    var entryActions = resolveActions.apply(stateClass.entry);
    var exitActions = resolveActions.apply(stateClass.exit);
    var whileActions = resolveActions.apply(stateClass.whilee);

    var parameters = new StateParameters(
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