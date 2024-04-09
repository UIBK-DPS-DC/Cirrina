package at.ac.uibk.dps.cirrina.core.object.statemachine;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.ACTION_NAME_IS_NOT_UNIQUE;
import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.ILLEGAL_STATE_MACHINE_GRAPH;
import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.MULTIPLE_TRANSITIONS_WITH_SAME_EVENT;
import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES;
import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.STATE_MACHINE_EXTENDS_INVALID;

import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.lang.classes.StateClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.StateMachineClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.transition.TransitionClass;
import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.ActionBuilder;
import at.ac.uibk.dps.cirrina.core.object.guard.Guard;
import at.ac.uibk.dps.cirrina.core.object.guard.GuardBuilder;
import at.ac.uibk.dps.cirrina.core.object.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.core.object.helper.GuardResolver;
import at.ac.uibk.dps.cirrina.core.object.state.State;
import at.ac.uibk.dps.cirrina.core.object.state.StateBuilder;
import at.ac.uibk.dps.cirrina.core.object.transition.TransitionBuilder;
import java.util.HashSet;
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

  private List<Guard> buildGuards() throws IllegalArgumentException {
    var guards = stateMachineClass.guards.stream()
        .map(guardClass -> GuardBuilder.from(guardClass).build())
        .toList();

    // Ensure that no duplicate entries exist
    var duplicates = new HashSet<Guard>();
    guards.stream()
        .filter(n -> !duplicates.add(n))
        .collect(Collectors.toSet()).forEach(action -> {
          throw new IllegalArgumentException(VerificationException.from(ACTION_NAME_IS_NOT_UNIQUE, action.getName()));
        });

    return guards;
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
        .map(actionClass -> ActionBuilder.from(actionClass, null /* TODO: Provide me */).build())
        .toList();

    // Ensure that no duplicate entries exist
    var duplicates = new HashSet<Action>();
    actions.stream()
        .filter(n -> !duplicates.add(n))
        .collect(Collectors.toSet()).forEach(action -> {
          throw new IllegalArgumentException(VerificationException.from(ACTION_NAME_IS_NOT_UNIQUE, action.getName()));
        });

    return actions;
  }

  /**
   * Builds all nested state machines contained in the state machine class.
   *
   * @return A list containing all nested state machines.
   * @throws IllegalArgumentException In case one nested state machine could not be built.
   */
  private List<StateMachine> buildNestedStateMachines() throws IllegalArgumentException {
    // Gather the nested state machines
    List<StateMachineClass> nestedStateMachinesClasses = stateMachineClass.states.stream()
        .filter(StateMachineClass.class::isInstance)
        .map(StateMachineClass.class::cast)
        .toList();

    // Build all nested state machines
    return nestedStateMachinesClasses.stream()
        .map(nestedStateMachineClass -> new StateMachineBuilder(nestedStateMachineClass, knownStateMachines).build())
        .toList();
  }

  /**
   * Builds a state machine which does not extend another state machine.
   *
   * @return The state machine.
   * @throws IllegalArgumentException In case the state machine could not be built.
   */
  private StateMachine buildBase() throws IllegalArgumentException {
    var guards = buildGuards();
    var actions = buildActions();
    var nestedStateMachines = buildNestedStateMachines();

    // TODO: Add parameters
    var stateMachine = new StateMachine(stateMachineClass.name, stateMachineClass.localContext, guards, actions,
        stateMachineClass.abstractt, nestedStateMachines);

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
   * Builds a state machine which extends another state machine given by its name.
   *
   * @param extendsName The name of the state machine to extend.
   * @return The state machine.
   * @throws IllegalArgumentException In case the state machine could not be built or the provided state machine name is not known.
   * @see ChildStateMachineBuilder
   */
  private StateMachine buildExtended(String extendsName)
      throws IllegalArgumentException {
    // Get the state machine to inherit from and throw an error if it does not exist
    var baseStateMachine = knownStateMachines.stream()
        .filter(knownStateMachine -> knownStateMachine.getName().equals(extendsName))
        .findFirst().orElseThrow(() -> new IllegalArgumentException(
            VerificationException.from(STATE_MACHINE_EXTENDS_INVALID, extendsName)));

    var guards = buildGuards();
    var actions = buildActions();
    var nestedStateMachines = buildNestedStateMachines();

    // Create the child state machine
    return ChildStateMachineBuilder.implement(stateMachineClass, baseStateMachine, guards, actions, nestedStateMachines).build();
  }

  /**
   * Builds the state machine.
   *
   * @return The state machine.
   * @throws IllegalArgumentException In case the state machine could not be built.
   */
  public StateMachine build() throws IllegalArgumentException {
    var stateMachine = stateMachineClass.extendss
        .map(this::buildExtended)
        .orElseGet(this::buildBase);

    // If the state machine is not abstract but has abstract states, throw an error
    if (!stateMachine.isAbstract() && stateMachine.vertexSet().stream().anyMatch(State::isAbstract)) {
      throw new IllegalArgumentException(
          VerificationException.from(NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES, stateMachineClass));
    }

    var guardResolver = new GuardResolver(stateMachine);
    var actionResolver = new ActionResolver(stateMachine);

    // Attempt to add edges
    stateMachineClass.states.stream()
        .filter(StateClass.class::isInstance)
        .map(StateClass.class::cast)
        .forEach(stateClass -> {
          // Acquire source node, this is expected to always succeed as we use the previously created state
          var source = stateMachine.findStateByName(stateClass.name).get();

          Consumer<List<? extends TransitionClass>> processTransitions = (on) -> {
            for (var transitionClass : on) {
              // Acquire the target node
              var target = stateMachine.findStateByName(transitionClass.target).get();

              // Attempt to add an edge to the state machine graph that resembles the transition
              if (!stateMachine.addEdge(source, target, TransitionBuilder.from(transitionClass, guardResolver, actionResolver).build())) {
                throw new IllegalArgumentException(
                    VerificationException.from(ILLEGAL_STATE_MACHINE_GRAPH, source.getName(), target.getName(), stateMachineClass));
              }
            }
          };

          // Ensure that "on" transitions have distinct events
          var hasDuplicateEdges = stateClass.on.stream()
              .collect(Collectors.groupingBy(transitionClass -> transitionClass.event, Collectors.counting())).entrySet().stream()
              .anyMatch(entry -> entry.getValue() > 1);

          if (hasDuplicateEdges) {
            throw new IllegalArgumentException(VerificationException.from(MULTIPLE_TRANSITIONS_WITH_SAME_EVENT, stateClass));
          }
          processTransitions.accept(stateClass.on);

          // Attempt to add edges corresponding to the "always" transitions, these transitions are optional
          processTransitions.accept(stateClass.always);
        });

    // Add the created state machine as a known state machine in this builder
    knownStateMachines.add(stateMachine);

    return stateMachine;
  }
}