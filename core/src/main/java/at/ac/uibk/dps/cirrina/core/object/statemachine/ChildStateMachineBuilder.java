package at.ac.uibk.dps.cirrina.core.object.statemachine;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES;
import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES;

import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.lang.classes.StateClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.StateMachineClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.context.ContextClass;
import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.guard.Guard;
import at.ac.uibk.dps.cirrina.core.object.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.core.object.state.State;
import at.ac.uibk.dps.cirrina.core.object.state.StateBuilder;
import at.ac.uibk.dps.cirrina.core.object.transition.OnTransition;
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
  private final List<Guard> namedGuards;
  private final List<Action> namedActions;

  private ChildStateMachineBuilder(StateMachineClass stateMachineClass, StateMachine baseStateMachine, List<Guard> namedGuards,
      List<Action> namedActions, List<StateMachine> nestedStateMachine) {
    this.stateMachineClass = stateMachineClass;
    this.baseStateMachine = baseStateMachine;
    this.namedGuards = new ArrayList<>(namedGuards);
    this.namedActions = new ArrayList<>(namedActions);
    this.nestedStateMachines = nestedStateMachine;
  }

  public static ChildStateMachineBuilder implement(StateMachineClass tthis, StateMachine base, List<Guard> guards, List<Action> actions,
      List<StateMachine> nestedStateMachine) {
    return new ChildStateMachineBuilder(tthis, base, guards, actions, nestedStateMachine);
  }

  /**
   * Merges two optional context classes into one, keeping the variables of the first context class in case of duplicates.
   *
   * @param contextClass The first context class.
   * @param baseContextClass The second context class.
   * @return The merged context class.
   */
  private static Optional<ContextClass> mergeContext(Optional<ContextClass> contextClass, Optional<ContextClass> baseContextClass) {
    if (contextClass.isEmpty()) {
      return baseContextClass;
    }
    if (baseContextClass.isEmpty()) {
      return contextClass;
    }

    // Create a new list of variables, containing all variables of the first context class
    var variables = new ArrayList<>(contextClass.get().variables);

    // Add all variables of the second context class which are not part of the first context class
    var variableNames = variables.stream()
        .map(variable -> variable.name)
        .toList();
    variables.addAll(baseContextClass.get().variables.stream()
        .filter(variable -> !variableNames.contains(variable.name))
        .toList());

    // Build a new context class
    var mergedContextClass = new ContextClass();
    mergedContextClass.variables = variables;
    return Optional.of(mergedContextClass);
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

    Optional<ContextClass> localContext = mergeContext(stateMachineClass.localContext, baseStateMachine.getLocalContextClass());
    var parameters = new StateMachine.Parameters(
        stateMachineClass.name,
        localContext,
        namedGuards,
        namedActions,
        stateMachineClass.abstractt,
        nestedStateMachines
    );

    var stateMachine = new StateMachine(parameters);

    addStates(stateMachine);
    addBaseEdges(stateMachine);

    return stateMachine;
  }

  /**
   * Adds missing named actions defined in the base state machine.
   */
  private void addBaseActions() {
    List<Action> baseActions = baseStateMachine.getNamedActions();

    if (namedActions.isEmpty() && !baseActions.isEmpty()) {
      namedActions.addAll(baseActions);
    } else if (!namedActions.isEmpty() && !baseActions.isEmpty()) {
      final List<Action> finalActions = namedActions;

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

    if (namedGuards.isEmpty() && !baseGuards.isEmpty()) {
      namedGuards.addAll(baseGuards);
    } else if (!namedGuards.isEmpty() && !baseGuards.isEmpty()) {
      final List<Guard> finalGuards = namedGuards;

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
          VerificationException.from(STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES, stateMachineClass));
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

      stateMachine.addVertex(StateBuilder.from(stateMachine.getId(), stateClass, actionResolver, childState).build());
    });

    // Add missing base states which were not overridden
    baseStateMachine.vertexSet().stream()
        .filter(state -> stateClasses.stream()
            .noneMatch(stateClass -> state.getName().equals(stateClass.name)))
        .forEach(state -> stateMachine.addVertex(StateBuilder.from(stateMachine.getId(), state).build()));
  }

  /**
   * Recreates the edges which were not overridden.
   *
   * @param stateMachine The state machine.
   */
  private void addBaseEdges(StateMachine stateMachine) throws IllegalArgumentException {
    var stateClasses = getStateClasses();

    // Recreate all base edges
    baseStateMachine.edgeSet()
        .forEach(transition -> {
          // Get the transition source from either the base or child state machine (if overridden)
          State source = baseStateMachine.getEdgeSource(transition);
          var overriddenSource = stateMachine.findStateByName(source.getName());
          source = overriddenSource.orElse(source);

          // Check if the transition is event based and overridden in the child state
          if (transition instanceof OnTransition onTransition) {
            final var sourceName = source.getName();
            boolean isOverriddenTransition = stateClasses.stream()
                .filter(stateClass -> stateClass.name.equals(sourceName))
                .findFirst()
                .map(stateClass -> stateClass.on.stream()
                    .anyMatch(onTransitionClass -> onTransitionClass.event.equals(onTransition.getEventName())))
                .orElse(false);

            // If overridden, skip recreating this edge
            if (isOverriddenTransition) {
              return;
            }
          }

          // Get the transition target from either the base or child state machine (if overridden)
          State target = baseStateMachine.getEdgeTarget(transition);
          var overriddenTarget = stateMachine.findStateByName(target.getName());
          target = overriddenTarget.orElse(target);

          // Recreate the transition
          var newTransition = TransitionBuilder.from(transition).build();

          // Add the edge
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
