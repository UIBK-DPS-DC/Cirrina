package at.ac.uibk.dps.cirrina.classes.statemachine;

import at.ac.uibk.dps.cirrina.classes.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.classes.helper.GuardResolver;
import at.ac.uibk.dps.cirrina.classes.state.StateClass;
import at.ac.uibk.dps.cirrina.classes.state.StateClassBuilder;
import at.ac.uibk.dps.cirrina.classes.transition.TransitionClassBuilder;
import at.ac.uibk.dps.cirrina.csml.description.StateDescription;
import at.ac.uibk.dps.cirrina.csml.description.StateMachineDescription;
import at.ac.uibk.dps.cirrina.csml.description.transition.TransitionDescription;
import at.ac.uibk.dps.cirrina.execution.object.action.Action;
import at.ac.uibk.dps.cirrina.execution.object.action.ActionBuilder;
import at.ac.uibk.dps.cirrina.execution.object.guard.Guard;
import at.ac.uibk.dps.cirrina.execution.object.guard.GuardBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * StateClass machine builder. Builds a state machine based on a state machine class.
 */
public final class StateMachineClassBuilder {

  /**
   * State machine description.
   */
  private final StateMachineDescription stateMachineDescription;

  /**
   * The collection of known state machine classes, used to acquire the base state machine class.
   */
  private final List<StateMachineClass> knownStateMachineClasses;

  /**
   * Initializes this builder instance.
   *
   * @param stateMachineDescription State machine description.
   */
  private StateMachineClassBuilder(StateMachineDescription stateMachineDescription) {
    this(stateMachineDescription, List.of());
  }

  /**
   * Initializes this builder instance.
   *
   * @param stateMachineDescription  State machine description.
   * @param knownStateMachineClasses Collection of known state machine classes.
   */
  private StateMachineClassBuilder(StateMachineDescription stateMachineDescription, List<StateMachineClass> knownStateMachineClasses) {
    this.stateMachineDescription = stateMachineDescription;
    this.knownStateMachineClasses = knownStateMachineClasses;
  }

  /**
   * Construct a builder from a state machine description.
   *
   * @param stateMachineDescription State machine description.
   * @return Builder.
   */
  public static StateMachineClassBuilder from(StateMachineDescription stateMachineDescription) {
    return new StateMachineClassBuilder(stateMachineDescription);
  }

  /**
   * Construct a builder from a state machine description.
   *
   * @param stateMachineDescription  State machine description.
   * @param knownStateMachineClasses Collection of known state machine classes.
   * @return Builder.
   */
  public static StateMachineClassBuilder from(
      StateMachineDescription stateMachineDescription,
      List<StateMachineClass> knownStateMachineClasses
  ) {
    return new StateMachineClassBuilder(stateMachineDescription, knownStateMachineClasses);
  }

  /**
   * Builds the collection of guards.
   * <p>
   * Guard names must be unique.
   *
   * @return Collection of guards.
   * @throws IllegalArgumentException If guard names are not unique.
   */
  private List<Guard> buildGuards() throws IllegalArgumentException {
    var guards = stateMachineDescription.guards.stream()
        .map(guardClass -> GuardBuilder.from(guardClass).build())
        .toList();

    // Ensure that no duplicate entries exist
    var duplicates = new HashSet<Guard>();

    guards.stream()
        .filter(n -> !duplicates.add(n))
        .collect(Collectors.toSet()).forEach(guard -> {
          throw new IllegalArgumentException("Guard name '%s' is not unique".formatted(guard.getName()));
        });

    return guards;
  }

  /**
   * Builds the collection of actions.
   * <p>
   * Action names must be unique.
   *
   * @return The list of named actions or empty.
   * @throws IllegalArgumentException If action names are not unique.
   */
  private List<Action> buildActions() throws IllegalArgumentException {
    // Construct the list of named actions of this state machine, or leave empty if no named actions are declared
    var actions = stateMachineDescription.actions.stream()
        .map(actionClass -> ActionBuilder.from(actionClass, null /* TODO: Provide me */).build())
        .toList();

    // Ensure that no duplicate entries exist
    var duplicates = new HashSet<Action>();
    actions.stream()
        .filter(n -> !duplicates.add(n))
        .collect(Collectors.toSet()).forEach(action -> {
          throw new IllegalArgumentException("Action name '%s' is not unique".formatted(action.getName()));
        });

    return actions;
  }

  /**
   * Builds all nested state machines contained in the state machine class.
   *
   * @return A list containing all nested state machines.
   * @throws IllegalArgumentException In case one nested state machine could not be built.
   */
  private List<StateMachineClass> buildNestedStateMachines() throws IllegalArgumentException {
    // Gather the nested state machines
    List<StateMachineDescription> nestedStateMachinesClasses = stateMachineDescription.states.stream()
        .filter(StateMachineDescription.class::isInstance)
        .map(StateMachineDescription.class::cast)
        .toList();

    // Build all nested state machines
    return nestedStateMachinesClasses.stream()
        .map(nestedStateMachineClass -> new StateMachineClassBuilder(nestedStateMachineClass, knownStateMachineClasses).build())
        .toList();
  }

  /**
   * Builds a state machine which does not extend another state machine.
   *
   * @return The state machine.
   * @throws IllegalArgumentException In case the state machine could not be built.
   */
  private StateMachineClass buildBase() throws IllegalArgumentException {
    var namedGuards = buildGuards();
    var namedActions = buildActions();
    var nestedStateMachines = buildNestedStateMachines();

    var parameters = new StateMachineClass.Parameters(
        stateMachineDescription.name,
        stateMachineDescription.localContext.orElse(null),
        namedGuards,
        namedActions,
        stateMachineDescription.abstractt,
        nestedStateMachines
    );

    var stateMachine = new StateMachineClass(parameters);

    var actionResolver = new ActionResolver(stateMachine);

    // Attempt to add vertices
    stateMachineDescription.states.stream()
        .filter(StateDescription.class::isInstance)
        .map(stateClass -> StateClassBuilder.from(stateMachine.getId(), (StateDescription) stateClass, actionResolver).build())
        .forEach(stateMachine::addVertex);

    return stateMachine;
  }

  /**
   * Builds a state machine which extends another state machine given by its name.
   *
   * @param extendsName The name of the state machine to extend.
   * @return The state machine.
   * @throws IllegalArgumentException In case the state machine could not be built or the provided state machine name is not known.
   * @see ChildStateMachineClassBuilder
   */
  private StateMachineClass buildExtended(String extendsName)
      throws IllegalArgumentException {
    // Get the state machine to inherit from and throw an error if it does not exist
    var baseStateMachine = knownStateMachineClasses.stream()
        .filter(knownStateMachine -> knownStateMachine.getName().equals(extendsName))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Cannot extend '%s', state machine does not exist".formatted(extendsName)));

    var guards = buildGuards();
    var actions = buildActions();
    var nestedStateMachines = buildNestedStateMachines();

    // Create the child state machine
    return ChildStateMachineClassBuilder.implement(stateMachineDescription, baseStateMachine, guards, actions, nestedStateMachines).build();
  }

  /**
   * Builds the state machine.
   *
   * @return The state machine.
   * @throws IllegalArgumentException If the state machine has declared abstract states, but the state machine is not abstract.
   * @throws IllegalArgumentException If the state machine has declared a transition between two states that is illegal.
   * @throws IllegalArgumentException If the state machine has declared a state with a non-deterministic outward transition.
   */
  public StateMachineClass build() throws IllegalArgumentException {
    var stateMachine = stateMachineDescription.extendss
        .map(this::buildExtended)
        .orElseGet(this::buildBase);

    // If the state machine is not abstract but has abstract states, throw an error
    if (!stateMachine.isAbstract() && stateMachine.vertexSet().stream().anyMatch(StateClass::isAbstract)) {
      throw new IllegalArgumentException(
          "States of '%s' are abstract but state machine is not abstract".formatted(stateMachineDescription.name));
    }

    var guardResolver = new GuardResolver(stateMachine);
    var actionResolver = new ActionResolver(stateMachine);

    // Attempt to add edges
    stateMachineDescription.states.stream()
        .filter(StateDescription.class::isInstance)
        .map(StateDescription.class::cast)
        .forEach(stateClass -> {
          // Acquire source node, this is expected to always succeed as we use the previously created state
          var sourceStateClass = stateMachine.findStateClassByName(stateClass.name).get();

          Consumer<List<? extends TransitionDescription>> processTransitions = (on) -> {
            for (var transitionClass : on) {
              // Acquire the target node, if the target is not provided, this is a self-transition
              var targetStateClass = transitionClass.target
                  .map(targetName -> stateMachine.findStateClassByName(targetName).get())
                  .orElse(sourceStateClass);

              // Attempt to add an edge to the state machine graph that resembles the transition
              if (!stateMachine.addEdge(sourceStateClass, targetStateClass,
                  TransitionClassBuilder.from(transitionClass, guardResolver, actionResolver).build())) {
                throw new IllegalArgumentException(
                    "The edge between states '%s' and '%s' is illegal in '%s'".formatted(sourceStateClass.getName(),
                        targetStateClass.getName(), stateMachineDescription.name));
              }
            }
          };

          // Ensure that "on" transitions have distinct events
          var hasDuplicateEdges = stateClass.on.stream()
              .collect(Collectors.groupingBy(transitionClass -> transitionClass.event, Collectors.counting())).entrySet().stream()
              .anyMatch(entry -> entry.getValue() > 1);

          // TODO: This is actually allowed, depending on the guard conditions
          /*if (hasDuplicateEdges) {
            throw new IllegalArgumentException(
                "Multiple outwards transitions with the same event in '%s'".formatted(stateMachineDescription.name));
          }*/

          processTransitions.accept(stateClass.on);

          // Attempt to add edges corresponding to the "always" transitions, these transitions are optional
          processTransitions.accept(stateClass.always);
        });

    // Add the created state machine as a known state machine in this builder
    knownStateMachineClasses.add(stateMachine);

    return stateMachine;
  }
}
