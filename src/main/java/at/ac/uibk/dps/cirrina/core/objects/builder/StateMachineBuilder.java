package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.Common;
import at.ac.uibk.dps.cirrina.core.objects.State;
import at.ac.uibk.dps.cirrina.core.objects.StateMachine;
import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.lang.checker.CheckerException;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateMachineClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.transitions.TransitionClass;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StateMachineBuilder {

  private final StateMachineClass stateMachineClass;

  private final List<StateMachine> knownStateMachines = new ArrayList<>();

  public StateMachineBuilder(StateMachineClass stateMachineClass) {
    this(stateMachineClass, new ArrayList<>());
  }

  public StateMachineBuilder(StateMachineClass stateMachineClass,
      List<StateMachine> knownStateMachines) {
    this.stateMachineClass = stateMachineClass;
  }

  private Optional<List<Action>> buildActions(StateMachineClass stateMachineClass)
      throws IllegalArgumentException {
    // Construct the list of named actions of this state machine, or leave empty if no named actions are declared
    var actions = stateMachineClass.actions.map(actionClasses -> actionClasses.stream()
        .map(actionClass -> new ActionBuilder(actionClass).build())
        .toList());

    // Ensure that no duplicate entries exist
    actions.ifPresent(a -> Common.getListDuplicates(a)
        .forEach(action -> {
          throw new IllegalArgumentException(
              new CheckerException(CheckerException.Message.ACTION_NAME_IS_NOT_UNIQUE,
                  action.name));
        }));

    return actions;
  }

  private StateMachine buildBase(StateMachineClass stateMachineClass)
      throws IllegalArgumentException {
    Optional<List<Action>> actions = buildActions(stateMachineClass);

    var stateMachine = new StateMachine(stateMachineClass.name, actions,
        stateMachineClass.isAbstract);

    var actionResolver = new ActionResolver(stateMachine);

    // Attempt to add vertices
    stateMachineClass.states.stream()
        .filter(StateClass.class::isInstance)
        .map(stateClass -> new StateBuilder((StateClass) stateClass, actionResolver,
            Optional.empty()).build())
        .forEach(stateMachine::addVertex);

    return stateMachine;
  }

  private StateMachine buildInherited(StateMachineClass stateMachineClass, String inheritName)
      throws IllegalArgumentException {
    // Get the state machine to inherit from and throw an error if it does not exist
    StateMachine inherited = knownStateMachines.stream()
        .filter(builtStateMachine -> builtStateMachine.getName().equals(inheritName))
        .findFirst().orElseThrow(() -> new IllegalArgumentException(
            new CheckerException(CheckerException.Message.STATE_MACHINE_INHERITS_FROM_INVALID,
                stateMachineClass.name, inheritName)));

    // Clone the state machine
    var stateMachine = inherited.cloneWithNameAndActions(stateMachineClass.name, Optional.of(
        new ArrayList<Action>())); // TODO: Merge actions from base, override or extend inherited named actions

    // Checks for overridden states
    List<StateClass> stateClasses = stateMachineClass.states.stream()
        .filter(StateClass.class::isInstance)
        .map(StateClass.class::cast)
        .toList();

    // Ensure that the overridden state can in fact be overridden
    var canOverrideState = stateClasses.stream()
        .anyMatch(stateClass -> stateMachine.vertexSet().stream()
            .anyMatch(state -> !state.isVirtual && !state.isAbstract && state.name.equals(
                stateClass.name)));

    if (canOverrideState) {
      throw new IllegalArgumentException(
          new CheckerException(CheckerException.Message.STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES,
              stateMachineClass.name));
    }

    // Checks for abstract states
    List<State> abstractStates = stateMachine.vertexSet().stream()
        .filter(state -> state.isAbstract).toList();

    // Ensure that everything that is abstract is overridden
    var isComplete = inherited.isAbstract && abstractStates.stream()
        .anyMatch(state -> stateClasses.stream()
            .noneMatch(stateClass -> state.name.equals(stateClass.name)));

    if (isComplete) {
      throw new IllegalArgumentException(
          new CheckerException(
              CheckerException.Message.STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES,
              stateMachineClass.name, inheritName));
    }

    // After checking abstract states for validity, remove all abstract states, so they can be re-added
    abstractStates.forEach(stateMachine::removeVertex);

    var actionResolver = new ActionResolver(stateMachine);

    // Attempt to add vertices
    stateClasses.stream()
        .map(stateClass -> new StateBuilder(stateClass, actionResolver,
            Optional.ofNullable(inherited.getStateByName(stateClass.name))).build())
        .forEach(stateMachine::addVertex);

    return stateMachine;
  }

  public StateMachine build() throws IllegalArgumentException {
    StateMachine stateMachine = stateMachineClass.inherit
        .map(inheritName -> buildInherited(stateMachineClass, inheritName))
        .orElseGet(() -> buildBase(stateMachineClass));

    // If the state machine is not abstract but has abstract states, throw an error
    if (!stateMachine.isAbstract && stateMachine.vertexSet().stream()
        .anyMatch(state -> state.isAbstract)) {
      throw new IllegalArgumentException(
          new CheckerException(
              CheckerException.Message.NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES,
              stateMachineClass.name));
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
              if (!stateMachine.addEdge(source, target,
                  new TransitionBuilder(transitionClass, actionResolver).build())) {
                throw new IllegalArgumentException(
                    new CheckerException(CheckerException.Message.ILLEGAL_STATE_MACHINE_GRAPH,
                        source.name, target.name));
              }
            }
          };

          // Attempt to add edges corresponding to the "on" transitions, these transitions are optional
          stateClass.on.ifPresent(
              on -> {
                // Ensure that "on" transitions have distinct events
                var hasDuplicateEdges = on.stream()
                    .collect(Collectors.groupingBy(
                        transitionClass -> transitionClass.event, Collectors.counting())
                    ).entrySet().stream()
                    .anyMatch(entry -> entry.getValue() > 1);

                if (hasDuplicateEdges) {
                  throw new IllegalArgumentException(
                      new CheckerException(
                          CheckerException.Message.MULTIPLE_TRANSITIONS_WITH_SAME_EVENT,
                          stateClass.name));
                }
                processTransitions.accept(on);
              }
          );

          // Attempt to add edges corresponding to the "always" transitions, these transitions are optional
          stateClass.always.ifPresent(always -> processTransitions.accept(always));
        });

    // Add the created state machine as a known state machine in this builder, we keep known state machines to resolve inheritance
    knownStateMachines.add(stateMachine);

    // Gather the nested state machines
    List<StateMachineClass> nestedStateMachines = stateMachineClass.states.stream()
        .filter(StateMachineClass.class::isInstance)
        .map(StateMachineClass.class::cast)
        .toList();

    // Add all nested state machines to the parent state machine
    stateMachine.nestedStateMachines.addAll(nestedStateMachines.stream()
        .map(nestedStateMachineClass -> new StateMachineBuilder(nestedStateMachineClass,
            knownStateMachines).build())
        .toList());

    return stateMachine;
  }
}