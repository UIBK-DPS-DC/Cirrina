package at.ac.uibk.dps.cirrina.classes.statemachine;

import at.ac.uibk.dps.cirrina.classes.state.StateClassBuilder;
import at.ac.uibk.dps.cirrina.classes.transition.TransitionClassBuilder;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.StateDescription;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.StateMachineDescription;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription.TransitionDescription;
import java.util.List;
import java.util.Optional;
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
   * Builds all nested state machines contained in the state machine class.
   *
   * @return A list containing all nested state machines.
   * @throws IllegalArgumentException In case one nested state machine could not be built.
   */
  private List<StateMachineClass> buildNestedStateMachines() throws IllegalArgumentException {
    // Gather the nested state machines
    List<StateMachineDescription> nestedStateMachinesClasses = stateMachineDescription.getStates().stream()
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
    var nestedStateMachines = buildNestedStateMachines();

    var parameters = new StateMachineClass.Parameters(
        stateMachineDescription.getName(),
        stateMachineDescription.getLocalContext(),
        nestedStateMachines
    );

    var stateMachine = new StateMachineClass(parameters);

    // Attempt to add vertices
    stateMachineDescription.getStates().stream()
        .filter(StateDescription.class::isInstance)
        .map(stateClass -> StateClassBuilder.from(stateMachine.getId(), (StateDescription) stateClass).build())
        .forEach(stateMachine::addVertex);

    return stateMachine;
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
    var stateMachine = buildBase();

    // Attempt to add edges
    stateMachineDescription.getStates().stream()
        .filter(StateDescription.class::isInstance)
        .map(StateDescription.class::cast)
        .forEach(stateClass -> {
          // Acquire source node, this is expected to always succeed as we use the previously created state
          var sourceStateClass = stateMachine.findStateClassByName(stateClass.getName()).get();

          Consumer<List<? extends TransitionDescription>> processTransitions = (on) -> {
            for (var transitionClass : on) {
              // Acquire the target node, if the target is not provided, this is a self-transition
              var targetStateClass = Optional.ofNullable(transitionClass.getTarget())
                  .map(targetName -> stateMachine.findStateClassByName(targetName).get())
                  .orElse(sourceStateClass);

              // Attempt to add an edge to the state machine graph that resembles the transition
              if (!stateMachine.addEdge(sourceStateClass, targetStateClass,
                  TransitionClassBuilder.from(transitionClass).build())) {
                throw new IllegalArgumentException(
                    "The edge between states '%s' and '%s' is illegal in '%s'".formatted(sourceStateClass.getName(),
                        targetStateClass.getName(), stateMachineDescription.getName()));
              }
            }
          };

          // Ensure that "on" transitions have distinct events
          var hasDuplicateEdges = stateClass.getOn().stream()
              .collect(Collectors.groupingBy(transitionClass -> transitionClass.getEvent(), Collectors.counting())).entrySet().stream()
              .anyMatch(entry -> entry.getValue() > 1);

          // TODO: This is actually allowed, depending on the guard conditions
          /*if (hasDuplicateEdges) {
            throw new IllegalArgumentException(
                "Multiple outwards transitions with the same event in '%s'".formatted(stateMachineDescription.name));
          }*/

          processTransitions.accept(stateClass.getOn());

          // Attempt to add edges corresponding to the "always" transitions, these transitions are optional
          processTransitions.accept(stateClass.getAlways());
        });

    // Add the created state machine as a known state machine in this builder
    knownStateMachineClasses.add(stateMachine);

    return stateMachine;
  }
}
