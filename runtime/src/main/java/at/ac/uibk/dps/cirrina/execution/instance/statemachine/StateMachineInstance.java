package at.ac.uibk.dps.cirrina.execution.instance.statemachine;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.ContextBuilder;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.core.object.event.EventListener;
import at.ac.uibk.dps.cirrina.core.object.state.State;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.core.object.transition.Transition;
import at.ac.uibk.dps.cirrina.execution.command.ActionCommand;
import at.ac.uibk.dps.cirrina.execution.command.ActionRaiseCommand;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import at.ac.uibk.dps.cirrina.execution.command.ExecutionContext;
import at.ac.uibk.dps.cirrina.execution.command.Scope;
import at.ac.uibk.dps.cirrina.execution.instance.state.StateInstance;
import at.ac.uibk.dps.cirrina.execution.instance.transition.TransitionInstance;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.runtime.Runtime;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class StateMachineInstance implements Runnable, EventListener, Scope {

  public static final String EVENT_DATA_VARIABLE_PREFIX = "$";

  private static final Logger logger = LogManager.getLogger();

  private final StateMachineInstanceId stateMachineInstanceId = new StateMachineInstanceId();

  private final TimeoutActionManager timeoutActionManager = new TimeoutActionManager();

  private final BlockingQueue<Event> eventQueue = new LinkedBlockingDeque<>();

  private final Runtime parentRuntime;

  private final StateMachine stateMachineObject;

  private final StateMachineInstance parentStateMachineInstance;

  private final ServiceImplementationSelector serviceImplementationSelector;

  private final StateMachineInstanceEventHandler stateMachineInstanceEventHandler;

  private final Context localContext;

  private final Map<String, StateInstance> stateInstances;

  private StateInstance activeState;

  /**
   * Initializes this state machine instance object. A state machine instance is associated with a state machine object that describes its
   * static structure.
   * <p>
   * A state machine instance is parented in a runtime.
   * <p>
   * A service implementation selector is provided that allows selecting between the service implementations accessible to this state
   * machine instance.
   * <p>
   * Additionally, a state machine instance may be parented in a state machine instance, forming a nested state machine instance.
   *
   * @param parentRuntime                 Parent runtime.
   * @param stateMachineObject            State machine object
   * @param serviceImplementationSelector Service implementation selector.
   * @param parentStateMachineInstance    Parent state machine instance or null.
   * @throws CirrinaException In case of error.
   * @thread Runtime.
   */
  public StateMachineInstance(
      Runtime parentRuntime,
      StateMachine stateMachineObject,
      ServiceImplementationSelector serviceImplementationSelector,
      @Nullable StateMachineInstance parentStateMachineInstance
  ) throws CirrinaException {
    this.parentRuntime = parentRuntime;
    this.stateMachineObject = stateMachineObject;
    this.parentStateMachineInstance = parentStateMachineInstance;

    this.serviceImplementationSelector = serviceImplementationSelector;

    this.stateMachineInstanceEventHandler = new StateMachineInstanceEventHandler(this, this.parentRuntime.getEventHandler());

    // Build the local context
    this.localContext = stateMachineObject.getLocalContextClass()
        .map(ContextBuilder::from)
        .orElseGet(ContextBuilder::from)
        .inMemoryContext()
        .build();

    // Construct state instances
    this.stateInstances = stateMachineObject.vertexSet().stream()
        .collect(Collectors.toMap(State::getName, state -> new StateInstance(state, this)));
  }

  /**
   * On event callback.
   *
   * @param event Received event.
   * @thread Events.
   */
  @Override
  public void onReceiveEvent(Event event) {
    // Nothing to do if the state machine is terminated
    if (isTerminated()) {
      return;
    }

    eventQueue.add(event);
  }

  @Override
  public Extent getExtent() {
    return Optional.ofNullable(parentStateMachineInstance)
        .map(parent -> parent.getExtent().extend(localContext))
        .orElseGet(() -> parentRuntime.getExtent().extend(localContext));
  }

  /**
   * Returns the state machine instance ID.
   *
   * @return State machine instance ID.
   */
  public StateMachineInstanceId getStateMachineInstanceId() {
    return stateMachineInstanceId;
  }

  /**
   * Returns the state machine object.
   *
   * @return State machine object.
   */
  public StateMachine getStateMachineObject() {
    return stateMachineObject;
  }

  private boolean isTerminated() {
    return activeState != null // The active state is null initially, this indicates that the state machine instance is not terminated
        && activeState.getStateObject().isTerminal();
  }

  private CommandFactory stateMachineScopedCommandFactory(StateMachineInstance stateMachineInstance) {
    return new CommandFactory(new ExecutionContext(
        stateMachineInstance,             // Scope
        serviceImplementationSelector,    // Service implementation selector
        stateMachineInstanceEventHandler, // Event handler
        this,                             // Event listener
        false                             // Is while?
    ));
  }

  private CommandFactory stateScopedCommandFactory(StateInstance stateInstance, boolean isWhile) {
    return new CommandFactory(new ExecutionContext(
        stateInstance,                    // Scope
        serviceImplementationSelector,    // Service implementation selector
        stateMachineInstanceEventHandler, // Event handler
        this,                             // Event listener
        false                             // Is while?
    ));
  }

  private Optional<StateInstance> findStateInstanceByName(String name) {
    return stateInstances.containsKey(name) ? Optional.of(stateInstances.get(name)) : Optional.empty();
  }

  private Optional<TransitionInstance> selectOnTransition(Event event) throws CirrinaException {
    // Find the transitions from the active state for the given event
    final var transitionObjects = stateMachineObject
        .findOnTransitionsFromStateByEventName(activeState.getStateObject(), event.getName());

    return selectTransition(transitionObjects);
  }

  private Optional<TransitionInstance> selectAlwaysTransition() throws CirrinaException {
    // Find the transitions from the active state for the given event
    final var transitionObjects = stateMachineObject
        .findAlwaysTransitionsFromState(activeState.getStateObject());

    return selectTransition(transitionObjects);
  }

  private Optional<TransitionInstance> selectTransition(List<? extends Transition> transitionObjects) throws CirrinaException {
    final var extent = getExtent();

    // A transition is taken when its guard conditions evaluate to true, or they do not evaluate to true, but an else target state is provided
    final var selectedTransitions = new ArrayList<TransitionInstance>();

    for (final var transitionObject : transitionObjects) {
      final var isElse = transitionObject.getElse().isPresent();
      final var result = transitionObject.evaluate(extent);

      if (isElse || result) {
        selectedTransitions.add(new TransitionInstance(transitionObject, isElse && !result));
      }
    }

    return switch (selectedTransitions.size()) {
      case 0 -> Optional.empty();
      case 1 -> Optional.of(selectedTransitions.getFirst());
      default -> throw CirrinaException.from("Non-determinism detected");
    };
  }

  private void execute(List<ActionCommand> actionCommands) throws CirrinaException {
    for (final var actionCommand : actionCommands) {
      execute(actionCommand.execute());
    }
  }

  private void startAllTimeoutActions(List<TimeoutAction> timeoutActions) throws CirrinaException {
    for (final var timeoutAction : timeoutActions) {
      // The evaluated delay value is required to be numeric
      final var delay = timeoutAction.getDelay().execute(getExtent());

      if (!(delay instanceof Number)) {
        throw CirrinaException.from("The delay expression '%s' did not evaluate to a numeric value", timeoutAction.getDelay());
      }

      // Create action command
      final var actionTimeoutCommand = stateMachineScopedCommandFactory(this).createActionCommand(timeoutAction.getAction());

      if (!(actionTimeoutCommand instanceof ActionRaiseCommand)) {
        throw CirrinaException.from("A timeout action must be a raise action");
      }

      // Acquire the name, which must be provided (otherwise it cannot be reset)
      final var actionName = timeoutAction.getName()
          .orElseThrow(() -> CirrinaException.from("A timeout action must have a name"));

      // Start the timeout task
      timeoutActionManager.start(actionName, (Number) delay, () -> {
        try {
          execute(List.of(actionTimeoutCommand));
        } catch (CirrinaException e) {
          logger.error("Failed to execute timeout command: {}", e.getMessage());
        }
      });
    }
  }

  private void stopTimeoutAction(String actionName) throws CirrinaException {
    timeoutActionManager.stop(actionName);
  }

  private void stopAllTimeoutActions() {
    timeoutActionManager.stopAll();
  }

  private void switchActiveState(StateInstance stateInstance) throws CirrinaException {
    if (!stateInstances.containsValue(stateInstance)) {
      throw CirrinaException.from(
          "A state with the name '%s' could not be found while attempting to set the new active state of state machine '%s'",
          stateInstance.getStateObject().getName(), stateMachineInstanceId);
    }

    // Update the active state
    activeState = stateInstance;
  }

  private void doExit(StateInstance exitingStateInstance) throws CirrinaException {
    // Gather action commands
    final var exitActionCommands = exitingStateInstance.getExitActionCommands(
        stateScopedCommandFactory(exitingStateInstance, false));

    // Stop timeout actions
    stopAllTimeoutActions();

    // TODO: Cancel while actions

    // Execute in order
    execute(exitActionCommands);
  }

  private void doTransition(TransitionInstance transitionInstance) throws CirrinaException {
    // Do not execute actions for else target transitions
    if (transitionInstance.isElse()) {
      return;
    }

    // Gather action commands
    final var transitionActionCommands = transitionInstance.getActionCommands(
        stateMachineScopedCommandFactory(this));

    // Execute in order
    execute(transitionActionCommands);
  }

  private Optional<TransitionInstance> doEnter(StateInstance enteringStateInstance) throws CirrinaException {
    // Gather action commands
    final var entryActionCommands = enteringStateInstance.getEntryActionCommands(
        stateScopedCommandFactory(enteringStateInstance, false));

    final var whileActionCommands = enteringStateInstance.getWhileActionCommands(
        stateScopedCommandFactory(enteringStateInstance, true));

    final var timeoutActionObjects = enteringStateInstance.getTimeoutActionObjects();

    // Execute in order
    execute(entryActionCommands);
    execute(whileActionCommands);

    // Start timeout actions
    startAllTimeoutActions(timeoutActionObjects);

    // Switch the active state to the entering state
    switchActiveState(enteringStateInstance);

    return selectAlwaysTransition();
  }

  private Optional<TransitionInstance> doEnterInitialState() throws CirrinaException {
    // Acquire the initial state instance
    final var initialStateInstance = stateInstances.get(stateMachineObject.getInitialState().getName());

    // Enter the initial state
    doEnter(initialStateInstance);

    // Switch the active state to the initial state
    switchActiveState(initialStateInstance);

    return selectAlwaysTransition();
  }

  private void handleInternalTransition(@NotNull TransitionInstance transitionInstance) throws CirrinaException {
    // Only perform the transition
    doTransition(transitionInstance);
  }

  private void handleExternalTransition(@NotNull TransitionInstance transitionInstance) throws CirrinaException {
    // Acquire the target state instance
    final var targetStateInstance = findStateInstanceByName(transitionInstance.getTargetStateName())
        .orElseThrow(() -> CirrinaException.from("Target state cannot be found in state machine"));

    // Exit the current state
    doExit(activeState);

    // Perform the transition
    doTransition(transitionInstance);

    // Enter the target state, if there is a follow-up transition, handle it recursively
    final var nextTransitionInstance = doEnter(targetStateInstance);

    if (nextTransitionInstance.isPresent()) {
      handleTransition(nextTransitionInstance.get());
    }
  }

  private void handleTransition(@NotNull TransitionInstance transitionInstance) throws CirrinaException {
    if (transitionInstance.isInternalTransition()) {
      handleInternalTransition(transitionInstance);
    } else {
      handleExternalTransition(transitionInstance);
    }
  }

  private Optional<TransitionInstance> handleEvent() throws InterruptedException, CirrinaException {
    // Wait for the next event, this call is blocking
    final var event = eventQueue.take();

    // Find a matching transition
    final var onTransition = selectOnTransition(event);

    // Set the event data
    onTransition.ifPresent(transitionInstance -> {
      try {
        for (var contextVariable : event.getData()) {
          getExtent().setOrCreate(EVENT_DATA_VARIABLE_PREFIX + contextVariable.name(), contextVariable.value());
        }
      } catch (CirrinaException e) {
        logger.error("Failed to set event data: {}", e.getMessage());
      }
    });

    return onTransition;
  }

  /**
   * Runs this operation.
   */
  @Override
  public void run() {
    try {
      var nextTransition = doEnterInitialState();

      while (!isTerminated()) {
        // Wait for a next event, if no transition is selected. No transition is selected initially if the initial state has no selectable
        // always transition or thereafter if we've handled the selected transition
        if (nextTransition.isEmpty()) {
          nextTransition = handleEvent();
        }

        // If a transition is selected, handle it. The transition will be handled recursively; any transition selected due to entering a
        // next state is handled recursively. Therefore, if we're done handling this transition, we indicate that we need to wait for a new
        // event again
        if (nextTransition.isPresent()) {
          handleTransition(nextTransition.get());

          nextTransition = Optional.empty();
        }
      }
    } catch (CirrinaException e) {
      logger.error("{} received a fatal error: {}", stateMachineInstanceId.toString(), e.getMessage());
    } catch (InterruptedException e) {
      logger.info("{} is interrupted", stateMachineInstanceId.toString());

      Thread.currentThread().interrupt();
    }

    logger.info("{} is done", stateMachineInstanceId.toString());
  }
}
