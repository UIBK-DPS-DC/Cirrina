package at.ac.uibk.dps.cirrina.core.objects.builder;

import static at.ac.uibk.dps.cirrina.lang.checker.CheckerException.Message.STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES;

import at.ac.uibk.dps.cirrina.core.objects.State;
import at.ac.uibk.dps.cirrina.core.objects.StateMachine;
import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.core.objects.transitions.OnTransition;
import at.ac.uibk.dps.cirrina.core.objects.transitions.Transition;
import at.ac.uibk.dps.cirrina.lang.checker.CheckerException;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateMachineClass;
import java.util.List;
import java.util.Optional;

/**
 * Child state machine builder. Builds a child state machine based on a state machine class and a parent state machine object.
 */
public final class ChildStateMachineBuilder {

  private final StateMachineClass stateMachineClass;
  private final StateMachine baseStateMachine;
  private List<Action> actions;

  private ChildStateMachineBuilder(StateMachineClass stateMachineClass, StateMachine base, List<Action> actions) {
    this.stateMachineClass = stateMachineClass;
    this.baseStateMachine = base;
    this.actions = actions;
  }

  public static ChildStateMachineBuilder implement(StateMachineClass tthis, StateMachine base, List<Action> actions) {
    return new ChildStateMachineBuilder(tthis, base, actions);
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

    addParentActions();

    StateMachine stateMachine = baseStateMachine.cloneWithStateMachineClass(stateMachineClass, actions);

    addStates(stateMachine);
    addParentEdges(stateMachine);

    return stateMachine;
  }

  /**
   * Adds missing actions from the parent state machine.
   */
  private void addParentActions() {
    List<Action> parentActions = baseStateMachine.getActions();

    if (actions.isEmpty() && !parentActions.isEmpty()) {
      actions = parentActions;
    } else if (!actions.isEmpty() && !parentActions.isEmpty()) {
      final List<Action> finalActions = actions;

      parentActions.stream()
          .filter(parentAction -> finalActions.stream()
              .noneMatch(action -> action.name.equals(parentAction.name)))
          .forEach(finalActions::add);
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
            .anyMatch(state -> !state.isVirtual && !state.isAbstract && state.name.equals(
                stateClass.name)));

    if (cannotOverrideState) {
      throw new IllegalArgumentException(
          CheckerException.from(CheckerException.Message.STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES, stateMachineClass.name));
    }
  }

  /**
   * Ensures that everything that is abstract is overridden.
   *
   * @throws IllegalArgumentException When at least one abstract state is not overridden.
   */
  private void checkAbstractStates() throws IllegalArgumentException {
    if (!baseStateMachine.isAbstract() || stateMachineClass.isAbstract) {
      return;
    }

    var stateClasses = getStateClasses();
    var abstractStates = baseStateMachine.vertexSet().stream()
        .filter(state -> state.isAbstract).toList();

    var isIncomplete = abstractStates.stream()
        .anyMatch(state -> stateClasses.stream()
            .noneMatch(stateClass -> state.name.equals(stateClass.name)));

    if (isIncomplete) {
      throw new IllegalArgumentException(
          CheckerException.from(STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES, stateMachineClass.name, baseStateMachine.getName()));
    }
  }

  /**
   * Adds states to the child state machine. Adding the not overridden parent states is necessary because
   * {@link StateMachine#cloneWithStateMachineClass} returns a shallow copy.
   *
   * @param stateMachine The state machine.
   */
  private void addStates(StateMachine stateMachine) {
    var stateClasses = getStateClasses();
    var actionResolver = new ActionResolver(stateMachine);

    // Add new states and overridden states
    stateClasses.forEach(stateClass -> {
      // If the state already exists in the parent, remove it and re-add a new state which is built using the child state
      Optional<State> childState = baseStateMachine.findStateByName(stateClass.name);
      childState.ifPresent(stateMachine::removeVertex);

      stateMachine.addVertex(StateBuilder.from(stateClass, actionResolver, childState).build());
    });

    // Add missing parent states which were not overridden
    baseStateMachine.vertexSet().stream()
        .filter(state -> stateClasses.stream()
            .noneMatch(stateClass -> state.name.equals(stateClass.name)))
        .forEach(stateMachine::addVertex);
  }

  /**
   * Recreates the edges. Recreating edges is necessary because {@link StateMachine#cloneWithStateMachineClass} returns a shallow copy.
   *
   * @param stateMachine The state machine.
   */
  private void addParentEdges(StateMachine stateMachine) {
    // Recreate all parent edges
    baseStateMachine.edgeSet()
        .forEach(transition -> {
          // Get the transition source and target from either the parent or child state machine (if overridden)
          State source = baseStateMachine.getEdgeSource(transition);
          var overriddenSource = stateMachine.findStateByName(source.name);
          source = overriddenSource.orElse(source);

          State target = baseStateMachine.getEdgeTarget(transition);
          var overriddenTarget = stateMachine.findStateByName(target.name);
          target = overriddenTarget.orElse(target);

          // Recreate the transition
          Transition newTransition = transition instanceof OnTransition
              ? new OnTransition(transition.target, transition.allActions(),
              ((OnTransition) transition).eventName)
              : new Transition(transition.target, transition.allActions());

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
