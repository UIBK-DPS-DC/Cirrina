package at.ac.uibk.dps.cirrina.core.objects;

import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.actions.RaiseAction;
import at.ac.uibk.dps.cirrina.core.objects.transitions.OnTransition;
import at.ac.uibk.dps.cirrina.core.objects.transitions.Transition;
import at.ac.uibk.dps.cirrina.lang.checker.CheckerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jgrapht.graph.DirectedPseudograph;

public class StateMachine extends DirectedPseudograph<State, Transition> {

  public final List<StateMachine> nestedStateMachines;

  public final boolean isAbstract;

  private String name;

  private Optional<List<Action>> actions;

  public StateMachine(String name, Optional<List<Action>> actions, boolean isAbstract) {
    super(Transition.class);

    this.name = name;
    this.actions = actions;
    this.isAbstract = isAbstract;
    this.nestedStateMachines = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  /**
   * Returns the collection of events handled by this state machine.
   *
   * @return Events handled by this state machine.
   */
  public List<String> getHandledEvents() {
    return edgeSet().stream()
        .filter(OnTransition.class::isInstance)
        .map(onTransition -> ((OnTransition) onTransition).eventName)
        .toList();
  }

  public List<Event> getRaisedEvents() {
    return Stream.concat(
            vertexSet().stream().flatMap(v -> v.getActionsOfType(RaiseAction.class).stream()),
            edgeSet().stream().flatMap(e -> e.getActionsOfType(RaiseAction.class).stream()))
        .map(raiseAction -> raiseAction.event)
        .toList();
  }

  /**
   * Returns a state by its name. If not one state is known with the supplied name, empty is returned.
   *
   * @param stateName Name of the state to return.
   * @return The state with the supplied name or empty.
   * @throws IllegalArgumentException In case not one state is known with the supplied name.
   */
  public State getStateByName(String stateName) {
    // Attempt to match the provided name to a known state
    var states = vertexSet().stream()
        .filter(state -> state.name.equals(stateName))
        .toList();

    // Expect precisely one state with the provided name
    if (states.isEmpty()) {
      throw new IllegalArgumentException(new CheckerException(CheckerException.Message.STATE_NAME_DOES_NOT_EXIST, stateName));
    } else if (states.size() != 1) {
      throw new IllegalArgumentException(new CheckerException(CheckerException.Message.STATE_NAME_IS_NOT_UNIQUE, stateName));
    }
    return states.getFirst();
  }

  /**
   * Returns an action by its name. If not one action is known with the supplied name, empty is returned.
   *
   * @param actionName Name of the action to return.
   * @return The action with the supplied name or empty.
   * @throws IllegalArgumentException In case not one action is known with the supplied name.
   */
  public Action getActionByName(String actionName) throws IllegalArgumentException {
    // Ensure that named actions are declared and an action with the provided name exists
    var actionsWithName = actions.orElseThrow(
            () -> new IllegalArgumentException(new CheckerException(CheckerException.Message.STATE_MACHINE_HAS_NO_NAMED_ACTIONS, name)))
        .stream()
        .filter(action -> action.name.equals(Optional.of(actionName)))
        .toList();

    // Ensure that precisely one action is known with this name
    if (actionsWithName.isEmpty()) {
      throw new IllegalArgumentException(new CheckerException(CheckerException.Message.NAMED_ACTION_DOES_NOT_EXIST, name, actionName));
    } else if (actionsWithName.size() != 1) {
      throw new IllegalArgumentException(new CheckerException(CheckerException.Message.ACTION_NAME_IS_NOT_UNIQUE, name, actionName));
    }
    return actionsWithName.getFirst();
  }

  public StateMachine cloneWithNameAndActions(String name, Optional<List<Action>> actions) {
    var stateMachine = (StateMachine) clone();

    stateMachine.name = name;
    stateMachine.actions = actions;

    return stateMachine;
  }

  @Override
  public String toString() {
    return name;
  }
}
