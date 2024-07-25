package at.ac.uibk.dps.cirrina.classes.statemachine;

import at.ac.uibk.dps.cirrina.classes.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.classes.state.StateClass;
import at.ac.uibk.dps.cirrina.classes.state.StateClassBuilder;
import at.ac.uibk.dps.cirrina.classes.transition.OnTransitionClass;
import at.ac.uibk.dps.cirrina.classes.transition.TransitionClassBuilder;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.ContextDescription;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.StateDescription;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.StateMachineDescription;
import at.ac.uibk.dps.cirrina.execution.object.action.Action;
import at.ac.uibk.dps.cirrina.execution.object.guard.Guard;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Child state machine builder. Builds a child state machine based on a state machine description and a base state machine object.
 */
public final class ChildStateMachineClassBuilder {

  /**
   * State machine description.
   */
  private final StateMachineDescription stateMachineDescription;

  /**
   * Base state machine class.
   */
  private final StateMachineClass baseStateMachineClass;

  /**
   * Collection of nested state machine classes being built.
   */
  private final List<StateMachineClass> nestedStateMachineClasses;

  /**
   * Collection of named guards being built.
   */
  private final List<Guard> namedGuards;

  /**
   * Collection of named actions being built.
   */
  private final List<Action> namedActions;

  private ChildStateMachineClassBuilder(
      StateMachineDescription stateMachineDescription,
      StateMachineClass baseStateMachineClass,
      List<Guard> namedGuards,
      List<Action> namedActions,
      List<StateMachineClass> nestedStateMachineClass
  ) {
    this.stateMachineDescription = stateMachineDescription;
    this.baseStateMachineClass = baseStateMachineClass;
    this.namedGuards = new ArrayList<>(namedGuards);
    this.namedActions = new ArrayList<>(namedActions);
    this.nestedStateMachineClasses = nestedStateMachineClass;
  }

  public static ChildStateMachineClassBuilder implement(
      StateMachineDescription tthis,
      StateMachineClass base,
      List<Guard> guards,
      List<Action> actions,
      List<StateMachineClass> nestedStateMachineClass
  ) {
    return new ChildStateMachineClassBuilder(tthis, base, guards, actions, nestedStateMachineClass);
  }

  /**
   * Merges two optional context description into one, keeping the variables of the first context description in case of duplicates.
   *
   * @param contextDescription     The first context description.
   * @param baseContextDescription The second context description.
   * @return The merged context description.
   */
  private static Optional<ContextDescription> mergeContext(
      @Nullable ContextDescription contextDescription,
      @Nullable ContextDescription baseContextDescription
  ) {
    // If either the first or second context description is null, skip merging and return the other one
    if (contextDescription == null) {
      return Optional.ofNullable(baseContextDescription);
    }
    if (baseContextDescription == null) {
      return Optional.of(contextDescription);
    }

    // Create a new list of variables, containing all variables of the first context description
    final var variables = new ArrayList<>(contextDescription.getVariables());

    // Add all variables of the second context description which are not part of the first context description
    final var variableNames = variables.stream()
        .map(variable -> variable.getName())
        .toList();

    variables.addAll(
        baseContextDescription.getVariables().stream()
            .filter(variable -> !variableNames.contains(variable.getName()))
            .toList()
    );

    // Build a new context description
    var mergedContextDescription = new ContextDescription(variables);

    return Optional.of(mergedContextDescription);
  }

  /**
   * Builds the child state machine.
   *
   * @return The child state machine.
   * @throws IllegalArgumentException In case the state machine could not be built.
   */
  public StateMachineClass build() throws IllegalArgumentException {
    // Re-add named actions and guards of the base state machine
    addBaseActions();
    addBaseGuards();

    // Merge local context variables of the base and child state machine's local contexts
    final var localContext = mergeContext(
        stateMachineDescription.getLocalContext(),
        baseStateMachineClass.getLocalContextClass().orElse(null)
    );

    final var parameters = new StateMachineClass.Parameters(
        stateMachineDescription.getName(),
        localContext.orElse(null),
        namedGuards,
        namedActions,
        nestedStateMachineClasses
    );

    final var stateMachine = new StateMachineClass(parameters);

    // Add new states, overridden states and states which are defined in the base state machine only
    addStates(stateMachine);

    // Add all edges of the base state machine which were not overridden
    addBaseEdges(stateMachine);

    return stateMachine;
  }

  /**
   * Adds missing named actions defined in the base state machine.
   */
  private void addBaseActions() {
    final List<Action> baseActions = baseStateMachineClass.getNamedActions();

    if (namedActions.isEmpty() && !baseActions.isEmpty()) {
      // Add all action of the base state machine if this state machine has no named actions
      namedActions.addAll(baseActions);
    } else if (!namedActions.isEmpty() && !baseActions.isEmpty()) {
      // Add all actions of the base state machine which are not overridden by this state machine
      baseActions.stream()
          .filter(baseAction -> namedActions.stream()
              .noneMatch(action -> action.getName().equals(baseAction.getName()))
          )
          .forEach(namedActions::add);
    }
  }

  /**
   * Adds missing named guards defined in the base state machine.
   */
  private void addBaseGuards() {
    final List<Guard> baseGuards = baseStateMachineClass.getNamedGuards();

    if (namedGuards.isEmpty() && !baseGuards.isEmpty()) {
      // Add all guards of the base state machine if this state machine has no named guards
      namedGuards.addAll(baseGuards);
    } else if (!namedGuards.isEmpty() && !baseGuards.isEmpty()) {
      // Add all guards of the base state machine which are not overridden by this state machine
      baseGuards.stream()
          .filter(baseGuard -> namedGuards.stream()
              .noneMatch(guard -> guard.getName().equals(baseGuard.getName()))
          )
          .forEach(namedGuards::add);
    }
  }

  /**
   * Adds states to the child state machine.
   *
   * @param stateMachineClass The state machine class.
   */
  private void addStates(StateMachineClass stateMachineClass) throws IllegalArgumentException {
    final var stateDescriptions = getStateDescriptions();

    final var actionResolver = new ActionResolver(stateMachineClass);

    // Add new states and overridden states
    stateDescriptions.forEach(stateDescription -> {

      // Get the overridden state from the base state machine or null if the state is new and not overridden
      final var baseStateClass = baseStateMachineClass.findStateClassByName(stateDescription.getName())
          .orElse(null);

      // Add the overridden or new state
      stateMachineClass.addVertex(
          StateClassBuilder.from(stateMachineClass.getId(), stateDescription, actionResolver, baseStateClass).build());
    });

    // Add missing states of the base state machine which were not overridden
    baseStateMachineClass.vertexSet().stream()
        .filter(state -> stateDescriptions.stream()
            .noneMatch(stateClass -> state.getName().equals(stateClass.getName()))
        )
        .forEach(state -> stateMachineClass.addVertex(
            StateClassBuilder.from(stateMachineClass.getId(), state, namedActions).build())
        );
  }

  /**
   * Recreates the edges which were not overridden.
   *
   * @param stateMachineClass The state machine.
   */
  private void addBaseEdges(StateMachineClass stateMachineClass) throws IllegalArgumentException {
    final var stateDescriptions = getStateDescriptions();

    // Recreate all base edges
    baseStateMachineClass.edgeSet()
        .forEach(transition -> {

          // Get the transition source from either the base or child state machine (if overridden)
          StateClass source = baseStateMachineClass.getEdgeSource(transition);
          final var overriddenSource = stateMachineClass.findStateClassByName(source.getName());
          source = overriddenSource.orElse(source);

          // Check if the transition is event based and overridden in the child state
          if (transition instanceof OnTransitionClass onTransition) {
            final var sourceName = source.getName();

            // If overridden, skip recreating this edge
            final boolean isOverriddenTransition = stateDescriptions.stream()
                .filter(stateDescription -> stateDescription.getName().equals(sourceName))
                .findFirst()
                .map(stateDescription -> stateDescription.getOn().stream()
                    .anyMatch(onTransitionDescription -> onTransitionDescription.getEvent().equals(onTransition.getEventName()))
                )
                .orElse(false);

            if (isOverriddenTransition) {
              return;
            }
          }

          // Get the transition target from either the base or child state machine (if overridden)
          StateClass target = baseStateMachineClass.getEdgeTarget(transition);
          final var overriddenTarget = stateMachineClass.findStateClassByName(target.getName());
          target = overriddenTarget.orElse(target);

          // Recreate the transition
          final var newTransition = TransitionClassBuilder.from(transition, namedGuards).build();

          // Add the edge
          stateMachineClass.addEdge(source, target, newTransition);
        });
  }

  /**
   * Return states as StateDescription helper method
   */
  private List<StateDescription> getStateDescriptions() {
    return stateMachineDescription.getStates().stream()
        .filter(StateDescription.class::isInstance)
        .map(StateDescription.class::cast)
        .toList();
  }
}
