package at.ac.uibk.dps.cirrina.core.object.statemachine;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES;

import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.lang.classes.StateClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.StateMachineClass;
import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.guard.Guard;
import at.ac.uibk.dps.cirrina.core.object.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.core.object.state.State;
import at.ac.uibk.dps.cirrina.core.object.state.StateBuilder;
import at.ac.uibk.dps.cirrina.core.object.transition.Transition;
import at.ac.uibk.dps.cirrina.core.object.transition.TransitionBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Child state machine builder. Builds a child state machine based on a state machine class and a base state machine object.
 */
public final class ChildStateMachineBuilder {

  private final StateMachineClass stateMachineClass;
  private final StateMachine baseStateMachine;
  private final List<StateMachine> nestedStateMachines;
  private final List<Guard> guards;
  private final List<Action> actions;

  private ChildStateMachineBuilder(StateMachineClass stateMachineClass, StateMachine baseStateMachine, List<Guard> guards,
      List<Action> actions,
      List<StateMachine> nestedStateMachine) {
    this.stateMachineClass = stateMachineClass;
    this.baseStateMachine = baseStateMachine;
    this.guards = new ArrayList<>(guards);
    this.actions = new ArrayList<>(actions);
    this.nestedStateMachines = nestedStateMachine;
  }

  public static ChildStateMachineBuilder implement(StateMachineClass tthis, StateMachine base, List<Guard> guards, List<Action> actions,
      List<StateMachine> nestedStateMachine) {
    return new ChildStateMachineBuilder(tthis, base, guards, actions, nestedStateMachine);
  }

  /**
   * Builds the child state machine.
   *
   * @return The child state machine.
   * @throws IllegalArgumentException In case the state machine could not be built.
   */
  public StateMachine build() throws IllegalArgumentException {
    checkAbstractStates();
    checkOverriddenStates();

    addBaseActions();
    addBaseGuards();

    StateMachine stateMachine = new StateMachine(stateMachineClass.name, guards, actions, stateMachineClass.abstractt,
        nestedStateMachines);

    addStates(stateMachine);
    addBaseEdges(stateMachine);

    return stateMachine;
  }

  /**
   * Adds missing named actions defined in the base state machine.
   */
  private void addBaseActions() {
    List<Action> baseActions = baseStateMachine.getNamedActions();

    if (actions.isEmpty() && !baseActions.isEmpty()) {
      actions.addAll(baseActions);
    } else if (!actions.isEmpty() && !baseActions.isEmpty()) {
      final List<Action> finalActions = actions;

      baseActions.stream()
          .filter(baseAction -> finalActions.stream()
              .noneMatch(action -> action.getName().equals(baseAction.getName())))
          .forEach(finalActions::add);
    }
  }

  /**
   * Adds missing named guards defined in the base state machine.
   */
  private void addBaseGuards() {
    List<Guard> baseGuards = baseStateMachine.getNamedGuards();

    if (guards.isEmpty() && !baseGuards.isEmpty()) {
      guards.addAll(baseGuards);
    } else if (!guards.isEmpty() && !baseGuards.isEmpty()) {
      final List<Guard> finalGuards = guards;

      baseGuards.stream()
          .filter(baseGuard -> finalGuards.stream()
              .noneMatch(guard -> guard.getName().equals(baseGuard.getName())))
          .forEach(finalGuards::add);
    }
  }

  /**
   * Ensures that the overridden state can in fact be overridden.
   *
   * @throws IllegalArgumentException When at least one state is overridden but neither abstract nor virtual.
   */
  private void checkOverriddenStates() throws IllegalArgumentException {
    var stateClasses = getStateClasses();

    boolean cannotOverrideState = stateClasses.stream()
        .anyMatch(stateClass -> baseStateMachine.vertexSet().stream()
            .anyMatch(state -> !state.isVirtual() && !state.isAbstract() && state.getName().equals(
                stateClass.name)));

    if (cannotOverrideState) {
      throw new IllegalArgumentException(
          VerificationException.from(VerificationException.Message.STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES, stateMachineClass));
    }
  }

  /**
   * Ensures that everything that is abstract is overridden.
   *
   * @throws IllegalArgumentException When at least one abstract state is not overridden.
   */
  private void checkAbstractStates() throws IllegalArgumentException {
    if (!baseStateMachine.isAbstract() || stateMachineClass.abstractt) {
      return;
    }

    var stateClasses = getStateClasses();
    var abstractStates = baseStateMachine.vertexSet().stream()
        .filter(State::isAbstract).toList();

    var isIncomplete = abstractStates.stream()
        .anyMatch(state -> stateClasses.stream()
            .noneMatch(stateClass -> state.getName().equals(stateClass.name)));

    if (isIncomplete) {
      throw new IllegalArgumentException(
          VerificationException.from(STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES, stateMachineClass));
    }
  }

  /**
   * Adds states to the child state machine.
   *
   * @param stateMachine The state machine.
   */
  private void addStates(StateMachine stateMachine) throws IllegalArgumentException {
    var stateClasses = getStateClasses();
    var actionResolver = new ActionResolver(stateMachine);

    // Add new states and overridden states
    stateClasses.forEach(stateClass -> {
      // If the state already exists in the base, remove it and re-add a new state which is built using the child state
      Optional<State> childState = baseStateMachine.findStateByName(stateClass.name);
      childState.ifPresent(stateMachine::removeVertex);

      stateMachine.addVertex(StateBuilder.from(stateClass, actionResolver, childState).build());
    });

    // Add missing base states which were not overridden
    baseStateMachine.vertexSet().stream()
        .filter(state -> stateClasses.stream()
            .noneMatch(stateClass -> state.getName().equals(stateClass.name)))
        .forEach(stateMachine::addVertex);
  }

  /**
   * Recreates the edges.
   *
   * @param stateMachine The state machine.
   */
  private void addBaseEdges(StateMachine stateMachine) throws IllegalArgumentException {
    // Recreate all base edges
    baseStateMachine.edgeSet()
        .forEach(transition -> {
          // Get the transition source and target from either the base or child state machine (if overridden)
          State source = baseStateMachine.getEdgeSource(transition);
          var overriddenSource = stateMachine.findStateByName(source.getName());
          source = overriddenSource.orElse(source);

          State target = baseStateMachine.getEdgeTarget(transition);
          var overriddenTarget = stateMachine.findStateByName(target.getName());
          target = overriddenTarget.orElse(target);

          // Recreate the transition
          Transition newTransition = TransitionBuilder.from(transition).build();

          stateMachine.addEdge(source, target, newTransition);
        });
  }

  /**
   * State classes helper method
   */
  private List<StateClass> getStateClasses() {
    return stateMachineClass.states.stream()
        .filter(StateClass.class::isInstance)
        .map(StateClass.class::cast)
        .toList();
  }
}
