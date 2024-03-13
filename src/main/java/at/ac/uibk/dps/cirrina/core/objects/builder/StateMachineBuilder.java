package at.ac.uibk.dps.cirrina.core.objects.builder;

import static at.ac.uibk.dps.cirrina.lang.checker.CheckerException.Message.ACTION_NAME_IS_NOT_UNIQUE;
import static at.ac.uibk.dps.cirrina.lang.checker.CheckerException.Message.ILLEGAL_STATE_MACHINE_GRAPH;
import static at.ac.uibk.dps.cirrina.lang.checker.CheckerException.Message.MULTIPLE_TRANSITIONS_WITH_SAME_EVENT;
import static at.ac.uibk.dps.cirrina.lang.checker.CheckerException.Message.NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES;
import static at.ac.uibk.dps.cirrina.lang.checker.CheckerException.Message.STATE_MACHINE_INHERITS_FROM_INVALID;

import at.ac.uibk.dps.cirrina.core.Common;
import at.ac.uibk.dps.cirrina.core.objects.StateMachine;
import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.lang.checker.CheckerException;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateMachineClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.transitions.TransitionClass;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * State machine builder. Builds a state machine based on a state machine class. To resolve inheritance keeps a list of known state
 * machines.
 */
public final class StateMachineBuilder {

  private final StateMachineClass stateMachineClass;

  private final List<StateMachine> knownStateMachines;

  private StateMachineBuilder(StateMachineClass stateMachineClass) {
    this(stateMachineClass, List.of());
  }

  private StateMachineBuilder(StateMachineClass stateMachineClass, List<StateMachine> knownStateMachines) {
    this.stateMachineClass = stateMachineClass;
    this.knownStateMachines = knownStateMachines;
  }

  public static StateMachineBuilder from(StateMachineClass stateMachineClass) {
    return new StateMachineBuilder(stateMachineClass);
  }

  public static StateMachineBuilder from(StateMachineClass stateMachineClass, List<StateMachine> knownStateMachines) {
    return new StateMachineBuilder(stateMachineClass, knownStateMachines);
  }

  /**
   * Constructs the list of named actions of this state machine if named actions are declared. Also ensures that no duplicate entries
   * exist.
   *
   * @return The list of named actions or empty.
   * @throws IllegalArgumentException When at least one duplicate entry exists.
   */
  private List<Action> buildActions() throws IllegalArgumentException {
    // Construct the list of named actions of this state machine, or leave empty if no named actions are declared
    var actions = stateMachineClass.actions.stream()
        .map(actionClass -> ActionBuilder.from(actionClass).build())
        .collect(Collectors.toList());

    // Ensure that no duplicate entries exist
    Common.getListDuplicates(actions)
        .forEach(action -> {
          throw new IllegalArgumentException(CheckerException.from(ACTION_NAME_IS_NOT_UNIQUE, action.name));
        });

    return actions;
  }

  /**
   * Builds a state machine which does not inherit from another state machine.
   *
   * @return The state machine.
   * @throws IllegalArgumentException In case the state machine could not be built.
   */
  private StateMachine buildBase() throws IllegalArgumentException {
    var actions = buildActions();

    var stateMachine = new StateMachine(stateMachineClass.name, actions, stateMachineClass.isAbstract);

    var actionResolver = new ActionResolver(stateMachine);

    // Attempt to add vertices
    stateMachineClass.states.stream()
        .filter(StateClass.class::isInstance)
        .map(stateClass -> StateBuilder.from((StateClass) stateClass, actionResolver,
            Optional.empty()).build())
        .forEach(stateMachine::addVertex);

    return stateMachine;
  }

  /**
   * Builds a state machine which inherits from another state machine given by its name.
   *
   * @param inheritName The name of the state machine to inherit from.
   * @return The state machine.
   * @throws IllegalArgumentException In case the state machine could not be built or the provided state machine name is not known.
   * @see ChildStateMachineBuilder
   */
  private StateMachine buildChild(String inheritName)
      throws IllegalArgumentException {
    // Get the state machine to inherit from and throw an error if it does not exist
    // TODO: Felix, I think a better name is 'base'
    StateMachine parentStateMachine = knownStateMachines.stream()
        .filter(knownStateMachine -> knownStateMachine.getName().equals(inheritName))
        .findFirst().orElseThrow(() -> new IllegalArgumentException(
            CheckerException.from(STATE_MACHINE_INHERITS_FROM_INVALID, stateMachineClass.name, inheritName)));

    // Create the child state machine
    return ChildStateMachineBuilder.implement(stateMachineClass, parentStateMachine, buildActions()).build();
  }

  /**
   * Builds the state machine.
   *
   * @return The state machine.
   * @throws IllegalArgumentException In case the state machine could not be built.
   */
  public StateMachine build() throws IllegalArgumentException {
    StateMachine stateMachine = stateMachineClass.inherit
        .map(this::buildChild)
        .orElseGet(this::buildBase);

    // If the state machine is not abstract but has abstract states, throw an error
    if (!stateMachine.isAbstract() && stateMachine.vertexSet().stream().anyMatch(state -> state.isAbstract)) {
      throw new IllegalArgumentException(CheckerException.from(NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES, stateMachineClass.name));
    }

    var actionResolver = new ActionResolver(stateMachine);

    // Attempt to add edges
    stateMachineClass.states.stream()
        .filter(StateClass.class::isInstance)
        .map(StateClass.class::cast)
        .forEach(stateClass -> {
          // Acquire source node, this is expected to always succeed as we use the previously created state
          var source = stateMachine.getStateByName(stateClass.name);

          Consumer<List<? extends TransitionClass>> processTransitions = (on) -> {
            for (var transitionClass : on) {
              // Acquire the target node
              var target = stateMachine.getStateByName(transitionClass.target);

              // Attempt to add an edge to the state machine graph that resembles the transition
              if (!stateMachine.addEdge(source, target, TransitionBuilder.from(transitionClass, actionResolver).build())) {
                throw new IllegalArgumentException(CheckerException.from(ILLEGAL_STATE_MACHINE_GRAPH, source.name, target.name));
              }
            }
          };

          // Ensure that "on" transitions have distinct events
          var hasDuplicateEdges = stateClass.on.stream()
              .collect(Collectors.groupingBy(transitionClass -> transitionClass.event, Collectors.counting())).entrySet().stream()
              .anyMatch(entry -> entry.getValue() > 1);

          if (hasDuplicateEdges) {
            throw new IllegalArgumentException(CheckerException.from(MULTIPLE_TRANSITIONS_WITH_SAME_EVENT, stateClass.name));
          }
          processTransitions.accept(stateClass.on);

          // Attempt to add edges corresponding to the "always" transitions, these transitions are optional
          processTransitions.accept(stateClass.always);
        });

    // Add the created state machine as a known state machine in this builder
    knownStateMachines.add(stateMachine);

    // Gather the nested state machines
    List<StateMachineClass> nestedStateMachines = stateMachineClass.states.stream()
        .filter(StateMachineClass.class::isInstance)
        .map(StateMachineClass.class::cast)
        .toList();

    // Add all nested state machines to the parent state machine
    stateMachine.nestedStateMachines.addAll(nestedStateMachines.stream()
        .map(nestedStateMachineClass -> new StateMachineBuilder(nestedStateMachineClass, knownStateMachines).build())
        .toList());

    return stateMachine;
  }
}