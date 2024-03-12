package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.objects.State;
import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.ActionOrActionReferenceClass;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class StateBuilder {

  private final StateClass stateClass;

  private final ActionResolver actionResolver;

  private final Optional<State> parentState;

  public StateBuilder(StateClass stateClass, ActionResolver actionResolver,
      Optional<State> parentState) {
    this.stateClass = stateClass;
    this.actionResolver = actionResolver;
    this.parentState = parentState;
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

    // Create this state
    return parentState
        .map(parentState -> new State(parentState, entryActions, exitActions, whileActions,
            stateClass.isAbstract))
        .orElseGet(() -> new State(stateClass.name, entryActions, exitActions, whileActions,
            stateClass.isAbstract,
            stateClass.isVirtual));
  }
}
