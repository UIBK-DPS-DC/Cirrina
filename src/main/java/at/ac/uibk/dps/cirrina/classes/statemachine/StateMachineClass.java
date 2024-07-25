package at.ac.uibk.dps.cirrina.classes.statemachine;

import at.ac.uibk.dps.cirrina.classes.state.StateClass;
import at.ac.uibk.dps.cirrina.classes.transition.OnTransitionClass;
import at.ac.uibk.dps.cirrina.classes.transition.TransitionClass;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.ContextDescription;
import at.ac.uibk.dps.cirrina.execution.object.action.Action;
import at.ac.uibk.dps.cirrina.execution.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.execution.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.guard.Guard;
import at.ac.uibk.dps.cirrina.io.plantuml.Exportable;
import at.ac.uibk.dps.cirrina.io.plantuml.PlantUmlVisitor;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.jgrapht.graph.DirectedPseudograph;

/**
 * State machine class, represents the structure of a state machine.
 * <p>
 * A state machine is a graph consisting of state classes as vertices and transition classes as edges. An edge between two vertices (states)
 * represents a possible transition between the states.
 */
public final class StateMachineClass extends DirectedPseudograph<StateClass, TransitionClass> implements Exportable {

  /**
   * The state machine class ID.
   */
  private final UUID id = UUID.randomUUID();

  /**
   * The collection of nested state machine classes.
   */
  private final List<StateMachineClass> nestedStateMachineClasses;

  /**
   * The name.
   */
  private final String name;

  /**
   * The local context class, can be null in case no local context has been declared.
   */
  private final @Nullable ContextDescription localContextClass;

  /**
   * Collection of named guards.
   */
  private final List<Guard> namedGuards;

  /**
   * Collection of named actions.
   */
  private final List<Action> namedActions;

  /**
   * Initializes this state machine class instance.
   *
   * @param parameters Parameters.
   */
  StateMachineClass(Parameters parameters) {
    super(TransitionClass.class);

    this.name = parameters.name;
    this.localContextClass = parameters.localContextClass;
    this.namedGuards = Collections.unmodifiableList(parameters.namedGuards);
    this.namedActions = Collections.unmodifiableList(parameters.namedActions);
    this.nestedStateMachineClasses = Collections.unmodifiableList(parameters.nestedStateMachineClasses);
  }

  /**
   * Return a string representation.
   *
   * @return String representation.
   */
  @Override
  public String toString() {
    return name;
  }

  /**
   * PlantUML visitor accept.
   *
   * @param visitor PlantUML visitor.
   */
  @Override
  public void accept(PlantUmlVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * Returns a state by its name. If not one state is known with the supplied name, empty is returned.
   *
   * @param stateName Name of the state to return.
   * @return The state with the supplied name or empty.
   * @throws IllegalArgumentException In case multiple states were found for the supplied name.
   */
  public Optional<StateClass> findStateClassByName(String stateName) {
    // Attempt to match the provided name to a known state
    final var states = vertexSet().stream()
        .filter(state -> state.getName().equals(stateName))
        .toList();

    if (states.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(states.getFirst());
  }

  /**
   * Returns the transitions from a state that are triggered by a given event name.
   *
   * @param fromStateClass From state.
   * @param eventName      The event name.
   * @return The list of on-transitions.
   */
  public List<OnTransitionClass> findOnTransitionsFromStateByEventName(StateClass fromStateClass, String eventName) {
    return outgoingEdgesOf(fromStateClass).stream()
        .filter(OnTransitionClass.class::isInstance)
        .map(OnTransitionClass.class::cast)
        .filter(transition -> transition.getEventName().equals(eventName))
        .toList();
  }

  /**
   * Returns the transitions from a state that are not event-triggered.
   *
   * @param fromStateClass From state.
   * @return The list of always-transitions.
   */
  public List<TransitionClass> findAlwaysTransitionsFromState(StateClass fromStateClass) {
    return outgoingEdgesOf(fromStateClass).stream()
        .filter(transition -> !(transition instanceof OnTransitionClass))
        .toList();
  }

  /**
   * Returns a guard by its name. If not one guard is known with the supplied name, empty is returned.
   *
   * @param guardName Name of the guard to return.
   * @return The guard with the supplied name or empty.
   */
  public Optional<Guard> findGuardByName(String guardName) {
    return namedGuards.stream()
        .filter(guard -> guard.getName().equals(Optional.of(guardName)))
        .findFirst();
  }

  /**
   * Returns an action by its name. If not one action is known with the supplied name, empty is returned.
   *
   * @param actionName Name of the action to return.
   * @return The action with the supplied name or empty.
   */
  public Optional<Action> findActionByName(String actionName) {
    return namedActions.stream()
        .filter(action -> action.getName().equals(Optional.of(actionName)))
        .findFirst();
  }

  /**
   * Returns the collection of nested state machine classes.
   *
   * @return Nested state machine classes.
   */
  public List<StateMachineClass> getNestedStateMachineClasses() {
    return nestedStateMachineClasses;
  }

  /**
   * Returns the ID.
   *
   * @return ID.
   */
  public UUID getId() {
    return id;
  }

  /**
   * Returns the name.
   *
   * @return Name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the local context class or empty.
   *
   * @return Local context class or empty.
   */
  public Optional<ContextDescription> getLocalContextClass() {
    return Optional.ofNullable(localContextClass);
  }

  /**
   * Returns the named actions of this state machine.
   *
   * @return named actions.
   */
  public List<Action> getNamedActions() {
    return namedActions;
  }

  /**
   * Returns the named guards of this state machine.
   *
   * @return named guards.
   */
  public List<Guard> getNamedGuards() {
    return namedGuards;
  }

  /**
   * Returns the initial state of this state machine.
   *
   * @return Initial state.
   */
  public StateClass getInitialState() {
    return vertexSet().stream()
        .filter(StateClass::isInitial)
        .findFirst()
        .get();
  }

  /**
   * Returns the collection of events handled by this state machine.
   *
   * @return Events handled by this state machine.
   */
  public List<String> getInputEvents() {
    return edgeSet().stream()
        .filter(OnTransitionClass.class::isInstance)
        .map(onTransition -> ((OnTransitionClass) onTransition).getEventName())
        .toList();
  }

  /**
   * Returns the events that may be raised from this state.
   *
   * @return Output events.
   */
  public List<Event> getOutputEvents() {
    return Stream.concat(
        // Raise action events
        Stream.concat(
            vertexSet().stream().flatMap(v -> v.getActionsOfType(RaiseAction.class).stream()),
            edgeSet().stream().flatMap(e -> e.getActionsOfType(RaiseAction.class).stream())
        ).map(RaiseAction::getEvent),

        // Invoke action events
        Stream.concat(
            vertexSet().stream().flatMap(v -> v.getActionsOfType(InvokeAction.class).stream()),
            edgeSet().stream().flatMap(e -> e.getActionsOfType(InvokeAction.class).stream())
        ).flatMap(invokeAction -> invokeAction.getDone().stream())
    ).toList();
  }

  /**
   * Returns the collection of state names.
   *
   * @return State names.
   */
  public List<String> getStateNames() {
    return vertexSet().stream()
        .map(StateClass::getName)
        .toList();
  }

  /**
   * Returns the collection of named guard names.
   *
   * @return Named guard names.
   */
  public List<String> getNamedGuardNames() {
    return namedGuards.stream()
        .map(Guard::getName)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  /**
   * Returns the collection of named action names.
   *
   * @return Named action names.
   */
  public List<String> getNamedActionNames() {
    return namedActions.stream()
        .map(Action::getName)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  /**
   * Parameters for the construction of a state machine class.
   *
   * @param name                      Name.
   * @param localContextClass         Local context class or empty if none declared.
   * @param namedGuards               Named guards.
   * @param namedActions              Named actions.
   * @param abstractt                 Is abstract.
   * @param nestedStateMachineClasses Nested state machine classes.
   */
  record Parameters(String name,
                    @Nullable ContextDescription localContextClass,
                    List<Guard> namedGuards,
                    List<Action> namedActions,
                    List<StateMachineClass> nestedStateMachineClasses) {

  }
}
