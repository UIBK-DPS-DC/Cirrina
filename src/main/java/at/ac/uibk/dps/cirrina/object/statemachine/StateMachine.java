package at.ac.uibk.dps.cirrina.object.statemachine;

import static at.ac.uibk.dps.cirrina.exception.VerificationException.Message.ACTION_NAME_IS_NOT_UNIQUE;
import static at.ac.uibk.dps.cirrina.exception.VerificationException.Message.NAMED_ACTION_DOES_NOT_EXIST;
import static at.ac.uibk.dps.cirrina.exception.VerificationException.Message.STATE_MACHINE_DOES_NOT_HAVE_ONE_INITIAL_STATE;
import static at.ac.uibk.dps.cirrina.exception.VerificationException.Message.STATE_NAME_IS_NOT_UNIQUE;

import at.ac.uibk.dps.cirrina.exception.VerificationException;
import at.ac.uibk.dps.cirrina.object.action.Action;
import at.ac.uibk.dps.cirrina.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.object.event.Event;
import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.object.transition.OnTransition;
import at.ac.uibk.dps.cirrina.object.transition.Transition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jgrapht.graph.DirectedPseudograph;

/**
 * Represents a state machine object, obtained by building a state machine class. This object encapsulates the structure and necessary
 * information for executing a state machine using a state machine executor. Multiple executors can be associated with one state machine
 * object, creating several state machine <i>instances</i>.
 * <p>
 * Formally, a state machine is a directed pseudograph, where states are vertices and transitions are edges. It is a deterministic
 * finite-state transducer (Mealy machine), defined as \(\mathcal S = (\Sigma, \Gamma, S, s_0, \delta, F, E, \omega, \epsilon_t,
 * \epsilon_s)\) with:
 * <ul>
 *   <li><i>\(\Sigma\)</i>: the set of input events;</li>
 *   <li><i>\(\Gamma\)</i>: the set of output events;</li>
 *   <li><i>\(S\)</i>: the set of states;</li>
 *   <li><i>\(s_0\)</i>: the initial state (such that \(s_0 \in S\));</li>
 *   <li><i>\(E\)</i>: the set of side effects;</li>
 *   <li><i>\(\delta\)</i>: the state transition function \(\delta: S\times \Sigma \rightarrow S\);</li>
 *   <li><i>\(\omega\)</i>: the output function \(\omega: S\times \Sigma \rightarrow \Gamma\);</li>
 *   <li><i>\(\epsilon_t\)</i>: the transition side effect function \(\epsilon_t: S \times \Sigma \rightarrow E\);</li>
 *   <li><i>\(\epsilon_s\)</i>: the state side effect function \(\epsilon_s: S \rightarrow E\).</li>
 * </ul>
 * <p>
 * This formalism defines a state machine as a system that processes input events, transitions between states, produces output events,
 * and triggers side effects during both transitions and in states. \(E\) are referred to as the actions of a collaborative state machine,
 * which can be declared named within a state machine or inline within a state/transition.
 */
public final class StateMachine extends DirectedPseudograph<State, Transition> {

  public final List<StateMachine> nestedStateMachines;

  private final String name;

  private final boolean isAbstract;

  private final List<Action> actions;

  StateMachine(String name, List<Action> actions, boolean isAbstract) {
    super(Transition.class);

    this.name = name;
    this.actions = Collections.unmodifiableList(actions);
    this.isAbstract = isAbstract;
    this.nestedStateMachines = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public boolean isAbstract() {
    return isAbstract;
  }

  public List<Action> getActions() {
    return actions;
  }

  /**
   * Returns the initial state of this state machine.
   *
   * @return Initial state.
   * @throws IllegalArgumentException If this state machine does not have exactly one initial state.
   */
  public State getInitialState() throws IllegalArgumentException {
    var initialStates = vertexSet().stream()
        .filter(state -> state.isInitial)
        .toList();

    if (initialStates.size() != 1) {
      throw new IllegalArgumentException(VerificationException.from(STATE_MACHINE_DOES_NOT_HAVE_ONE_INITIAL_STATE, getName()));
    }
    return initialStates.getFirst();
  }

  /**
   * Returns the collection of events handled by this state machine.
   *
   * @return Events handled by this state machine.
   */
  public List<String> getInputEvents() {
    return edgeSet().stream()
        .filter(OnTransition.class::isInstance)
        .map(onTransition -> ((OnTransition) onTransition).eventName)
        .toList();
  }

  public List<Event> getOutputEvents() {
    return Stream.concat(
        // Raise action events
        Stream.concat(
            vertexSet().stream().flatMap(v -> v.getActionsOfType(RaiseAction.class).stream()),
            edgeSet().stream().flatMap(e -> e.getActionsOfType(RaiseAction.class).stream())
        ).map(raiseAction -> raiseAction.event),

        // Invoke action events
        Stream.concat(
            vertexSet().stream().flatMap(v -> v.getActionsOfType(InvokeAction.class).stream()),
            edgeSet().stream().flatMap(e -> e.getActionsOfType(InvokeAction.class).stream())
        ).flatMap(invokeAction -> invokeAction.done.stream())
    ).toList();
  }

  /**
   * Returns a state by its name. If not one state is known with the supplied name, throw an error.
   *
   * @param stateName Name of the state to return.
   * @return The state with the supplied name.
   * @throws IllegalArgumentException In case not one state is known with the supplied name or multiple states were found.
   */
  public State getStateByName(String stateName) throws IllegalArgumentException {
    return findStateByName(stateName)
        .orElseThrow(() -> new IllegalArgumentException(
            VerificationException.from(VerificationException.Message.STATE_NAME_DOES_NOT_EXIST, stateName)));
  }

  /**
   * Returns a state by its name. If not one state is known with the supplied name, empty is returned.
   *
   * @param stateName Name of the state to return.
   * @return The state with the supplied name or empty.
   * @throws IllegalArgumentException In case multiple states were found for the supplied name.
   */
  public Optional<State> findStateByName(String stateName) throws IllegalArgumentException {
    // Attempt to match the provided name to a known state
    var states = vertexSet().stream()
        .filter(state -> state.name.equals(stateName))
        .toList();

    if (states.isEmpty()) {
      return Optional.empty();
    } else if (states.size() != 1) {
      throw new IllegalArgumentException(VerificationException.from(STATE_NAME_IS_NOT_UNIQUE, stateName));
    }
    return Optional.of(states.getFirst());
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
    var actionsWithName = actions.stream()
        .filter(action -> action.name.equals(Optional.of(actionName)))
        .toList();

    // Ensure that precisely one action is known with this name
    if (actionsWithName.isEmpty()) {
      throw new IllegalArgumentException(VerificationException.from(NAMED_ACTION_DOES_NOT_EXIST, name, actionName));
    } else if (actionsWithName.size() != 1) {
      throw new IllegalArgumentException(VerificationException.from(ACTION_NAME_IS_NOT_UNIQUE, name, actionName));
    }
    return actionsWithName.getFirst();
  }

  @Override
  public String toString() {
    return name;
  }
}
