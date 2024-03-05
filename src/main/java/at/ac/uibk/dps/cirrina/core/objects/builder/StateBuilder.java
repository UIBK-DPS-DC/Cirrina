package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.objects.State;
import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.ActionOrActionReferenceClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class StateBuilder {

  private final StateClass stateClass;

  private final ActionResolver actionResolver;

  private final Optional<State> inheritedState;

  public StateBuilder(StateClass stateClass, ActionResolver actionResolver, Optional<State> inheritedState) {
    this.stateClass = stateClass;
    this.actionResolver = actionResolver;
    this.inheritedState = inheritedState;
  }

  public State build() throws IllegalArgumentException {
    // Resolve actions
    Function<Optional<List<ActionOrActionReferenceClass>>, List<Action>> resolveActions = (Optional<List<ActionOrActionReferenceClass>> actions) ->
        actions.orElse(new ArrayList<ActionOrActionReferenceClass>()).stream()
            .map(actionResolver::resolve)
            .toList();

    var entryActions = resolveActions.apply(stateClass.entry);
    var exitActions = resolveActions.apply(stateClass.exit);
    var whileActions = resolveActions.apply(stateClass.whilee);

    // Create this state
    State state = inheritedState
        .map(State::new)
        .orElseGet(() -> new State(stateClass.name, entryActions, exitActions, whileActions, stateClass.isAbstract,
            stateClass.isVirtual));

    return state;
  }
}
