package at.ac.uibk.dps.cirrina.object.statemachine;

import static at.ac.uibk.dps.cirrina.exception.VerificationException.Message.STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES;

import at.ac.uibk.dps.cirrina.exception.VerificationException;
import at.ac.uibk.dps.cirrina.lang.classes.StateClass;
import at.ac.uibk.dps.cirrina.lang.classes.StateMachineClass;
import at.ac.uibk.dps.cirrina.object.action.Action;
import at.ac.uibk.dps.cirrina.object.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.object.state.StateBuilder;
import at.ac.uibk.dps.cirrina.object.transition.Transition;
import at.ac.uibk.dps.cirrina.object.transition.TransitionBuilder;
import java.util.List;
import java.util.Optional;

/**
 * Child state machine builder. Builds a child state machine based on a state machine class and a base state machine object.
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

    addBaseActions();

    StateMachine stateMachine = new StateMachine(stateMachineClass.name, actions, stateMachineClass.isAbstract);

    addStates(stateMachine);
    addBaseEdges(stateMachine);

    return stateMachine;
  }

  /**
   * Adds missing actions from the base state machine.
   */
  private void addBaseActions() {
    List<Action> baseActions = baseStateMachine.getActions();

    if (actions.isEmpty() && !baseActions.isEmpty()) {
      actions = baseActions;
    } else if (!actions.isEmpty() && !baseActions.isEmpty()) {
      final List<Action> finalActions = actions;

      baseActions.stream()
          .filter(baseAction -> finalActions.stream()
              .noneMatch(action -> action.name.equals(baseAction.name)))
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
          VerificationException.from(VerificationException.Message.STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES, stateMachineClass.name));
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
          VerificationException.from(STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES, stateMachineClass.name, baseStateMachine.getName()));
    }
  }

  /**
   * Adds states to the child state machine. Adding the not overridden base states is necessary because
   * {@link StateMachine#cloneWithStateMachineClass} returns a shallow copy.
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
            .noneMatch(stateClass -> state.name.equals(stateClass.name)))
        .forEach(stateMachine::addVertex);
  }

  /**
   * Recreates the edges. Recreating edges is necessary because {@link StateMachine#cloneWithStateMachineClass} returns a shallow copy.
   *
   * @param stateMachine The state machine.
   */
  private void addBaseEdges(StateMachine stateMachine) throws IllegalArgumentException {
    // Recreate all base edges
    baseStateMachine.edgeSet()
        .forEach(transition -> {
          // Get the transition source and target from either the base or child state machine (if overridden)
          State source = baseStateMachine.getEdgeSource(transition);
          var overriddenSource = stateMachine.findStateByName(source.name);
          source = overriddenSource.orElse(source);

          State target = baseStateMachine.getEdgeTarget(transition);
          var overriddenTarget = stateMachine.findStateByName(target.name);
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
