package at.ac.uibk.dps.cirrina.execution.object.statemachine;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_ACTION_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_EVENT_CHANNEL;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_EVENT_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_EVENT_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_TRANSITION_TARGET_STATE_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_TRANSITION_TYPE;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.EVENT_RECEIVED_EVENT;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_EXECUTED_ACTIONS;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_EXTERNAL_TRANSITIONS;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_HANDLED_EVENTS;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_INTERNAL_TRANSITIONS;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_RECEIVED_EVENTS;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_DO_ENTER;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_DO_EXIT;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_DO_TRANSITION;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_RUN;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_TIMEOUT;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_TRANSITION;

import at.ac.uibk.dps.cirrina.classes.state.StateClass;
import at.ac.uibk.dps.cirrina.classes.statemachine.StateMachineClass;
import at.ac.uibk.dps.cirrina.classes.transition.TransitionClass;
import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.execution.command.ActionCommand;
import at.ac.uibk.dps.cirrina.execution.command.ActionRaiseCommand;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import at.ac.uibk.dps.cirrina.execution.command.ExecutionContext;
import at.ac.uibk.dps.cirrina.execution.command.Scope;
import at.ac.uibk.dps.cirrina.execution.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.execution.object.context.Context;
import at.ac.uibk.dps.cirrina.execution.object.context.ContextBuilder;
import at.ac.uibk.dps.cirrina.execution.object.context.Extent;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.event.EventListener;
import at.ac.uibk.dps.cirrina.execution.object.state.State;
import at.ac.uibk.dps.cirrina.execution.object.transition.Transition;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.runtime.Runtime;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
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

public final class StateMachine implements Runnable, EventListener, Scope {

  public static final String EVENT_DATA_VARIABLE_PREFIX = "$";

  private static final Logger logger = LogManager.getLogger();

  private final StateMachineId stateMachineId = new StateMachineId();

  private final TimeoutActionManager timeoutActionManager = new TimeoutActionManager();

  private final BlockingQueue<Event> eventQueue = new LinkedBlockingDeque<>();

  private final Runtime parentRuntime;

  private final StateMachineClass stateMachineClassObject;

  private final ServiceImplementationSelector serviceImplementationSelector;

  private final OpenTelemetry openTelemetry;

  private final StateMachine parentStateMachine;

  private final Tracer tracer;

  private final Meter meter;

  private final StateMachineEventHandler stateMachineEventHandler;

  private final Context localContext;

  private final Map<String, State> stateInstances;

  private State activeState;

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
   * @param stateMachineClassObject       StateClass machine object
   * @param serviceImplementationSelector Service implementation selector.
   * @param parentStateMachine            Parent state machine instance or null.
   * @throws CirrinaException In case of error.
   * @thread Runtime.
   */
  public StateMachine(
      Runtime parentRuntime,
      StateMachineClass stateMachineClassObject,
      ServiceImplementationSelector serviceImplementationSelector,
      OpenTelemetry openTelemetry,
      @Nullable StateMachine parentStateMachine
  ) throws CirrinaException {
    this.parentRuntime = parentRuntime;
    this.stateMachineClassObject = stateMachineClassObject;
    this.serviceImplementationSelector = serviceImplementationSelector;
    this.openTelemetry = openTelemetry;
    this.parentStateMachine = parentStateMachine;

    final var instrumentationScopeName = String.format("stateMachine-%s", stateMachineId.toString());

    // Create an OpenTelemetry tracer
    tracer = this.openTelemetry.getTracer(instrumentationScopeName);

    // Create an OpenTelemetry meter
    meter = this.openTelemetry.getMeter(instrumentationScopeName);

    stateMachineEventHandler = new StateMachineEventHandler(this, this.parentRuntime.getEventHandler());

    // Build the local context
    localContext = stateMachineClassObject.getLocalContextClass()
        .map(ContextBuilder::from)
        .orElseGet(ContextBuilder::from)
        .inMemoryContext()
        .build();

    // Construct state instances
    stateInstances = stateMachineClassObject.vertexSet().stream()
        .collect(Collectors.toMap(StateClass::getName, state -> new State(state, this)));
  }

  /**
   * Handles a received event.
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

  /**
   * Returns this scope's extent.
   *
   * @return Extent.
   */
  @Override
  public Extent getExtent() {
    return Optional.ofNullable(parentStateMachine)
        .map(parent -> parent.getExtent().extend(localContext))
        .orElseGet(() -> parentRuntime.getExtent().extend(localContext));
  }

  /**
   * Returns the state machine instance ID.
   *
   * @return StateClass machine instance ID.
   */
  public StateMachineId getStateMachineInstanceId() {
    return stateMachineId;
  }

  /**
   * Returns the state machine object.
   *
   * @return StateClass machine object.
   */
  public StateMachineClass getStateMachineObject() {
    return stateMachineClassObject;
  }

  /**
   * Returns a flag that indicates if this state machine instance is terminated.
   *
   * @return True if the state machine instance is terminated, otherwise false.
   */
  public boolean isTerminated() {
    // The active state is null initially, this indicates that the state machine instance is not terminated
    if (activeState == null) {
      return false;
    }
    // The state machine instance should be terminated if it is nested and its parent is terminated.
    if (parentStateMachine != null && parentStateMachine.isTerminated()) {
      return true;
    }

    return activeState.getStateObject().isTerminal();
  }

  /**
   * Creates a state machine instance-scoped command factory.
   *
   * @param stateMachine StateClass machine instance (scope).
   * @return Command factory.
   */
  private CommandFactory stateMachineScopedCommandFactory(StateMachine stateMachine) {
    return new CommandFactory(new ExecutionContext(
        stateMachine,             // Scope
        serviceImplementationSelector,    // Service implementation selector
        stateMachineEventHandler, // Event handler
        this,                             // Event listener
        false                             // Is while?
    ));
  }

  /**
   * Creates a state instance-scoped command factory.
   *
   * @param state   StateClass instance (scope).
   * @param isWhile Whether the command factory build while actions.
   * @return Command factory.
   */
  private CommandFactory stateScopedCommandFactory(State state, boolean isWhile) {
    return new CommandFactory(new ExecutionContext(
        state,                    // Scope
        serviceImplementationSelector,    // Service implementation selector
        stateMachineEventHandler, // Event handler
        this,                             // Event listener
        false                             // Is while?
    ));
  }

  /**
   * Attempts to return a state instance by name.
   *
   * @param name Name to find.
   * @return StateClass instance or empty.
   */
  private Optional<State> findStateInstanceByName(String name) {
    return stateInstances.containsKey(name) ? Optional.of(stateInstances.get(name)) : Optional.empty();
  }

  /**
   * Attempts to select an on transition based on an event.
   *
   * @param event Event to find an on transition for.
   * @return On transition or empty in case no matching on transition can be selected.
   * @throws CirrinaException In case of non-determinism or any other error.
   */
  private Optional<Transition> trySelectOnTransition(Event event) throws CirrinaException {
    // Find the transitions from the active state for the given event
    final var transitionObjects = stateMachineClassObject
        .findOnTransitionsFromStateByEventName(activeState.getStateObject(), event.getName());

    return trySelectTransition(transitionObjects);
  }

  /**
   * Attempts to select an always transition.
   *
   * @return Always transition or empty in case no always transition can be selected.
   * @throws CirrinaException In case of non-determinism or any other error.
   */
  private Optional<Transition> trySelectAlwaysTransition() throws CirrinaException {
    // Find the transitions from the active state for the given event
    final var transitionObjects = stateMachineClassObject
        .findAlwaysTransitionsFromState(activeState.getStateObject());

    return trySelectTransition(transitionObjects);
  }

  /**
   * Attempts to select a transition based on a collection of transition objects.
   * <p>
   * A transition is selected if it its guards evaluate to true, or if it does not, and it has an else target state.
   *
   * @param transitionObjects Collection of transition objects to select from.
   * @return Selected transition instance or empty in case no transition can be selected.
   * @throws CirrinaException In case of non-determinism or any other error.
   */
  private Optional<Transition> trySelectTransition(List<? extends TransitionClass> transitionObjects)
      throws CirrinaException {
    final var extent = getExtent();

    // A transition is taken when its guard conditions evaluate to true, or they do not evaluate to true, but an else target state is provided
    final var selectedTransitions = new ArrayList<Transition>();

    for (final var transitionObject : transitionObjects) {
      final var isElse = transitionObject.getElse().isPresent();
      final var result = transitionObject.evaluate(extent);

      if (isElse || result) {
        selectedTransitions.add(new Transition(transitionObject, isElse && !result));
      }
    }

    return switch (selectedTransitions.size()) {
      case 0 -> Optional.empty();
      case 1 -> Optional.of(selectedTransitions.getFirst());
      default -> throw CirrinaException.from("Non-determinism detected");
    };
  }

  /**
   * Executes this collection of commands provided.
   * <p>
   * Commands are executed recursively. Any command that is the result of a command execution is executed immediately following the command
   * that created it.
   *
   * @param actionCommands Commands to execute.
   * @param parentSpan     Parent span.
   * @throws CirrinaException In case of error.
   */
  private void execute(List<ActionCommand> actionCommands, Span parentSpan) throws CirrinaException {
    for (final var actionCommand : actionCommands) {
      // Increment executed actions counter
      meter.counterBuilder(METRIC_EXECUTED_ACTIONS).build().add(1);

      // Execute this command
      final var newCommands = actionCommand.execute(tracer, parentSpan);

      // Execute any subsequent command
      execute(newCommands, parentSpan);
    }
  }

  /**
   * Starts all timeout actions as provided.
   * <p>
   * Timeout actions are executed until stopped.
   *
   * @param timeoutActionObjects Timeout action objects to start.
   * @param parentSpan           Parent span.
   * @throws CirrinaException In case of error.
   */
  private void startAllTimeoutActions(List<TimeoutAction> timeoutActionObjects, Span parentSpan) throws CirrinaException {
    for (final var timeoutActionObject : timeoutActionObjects) {
      // The evaluated delay value is required to be numeric
      final var delay = timeoutActionObject.getDelay().execute(getExtent());

      if (!(delay instanceof Number)) {
        throw CirrinaException.from("The delay expression '%s' did not evaluate to a numeric value", timeoutActionObject.getDelay());
      }

      // Create action command
      final var actionTimeoutCommand = stateMachineScopedCommandFactory(this).createActionCommand(timeoutActionObject.getAction());

      if (!(actionTimeoutCommand instanceof ActionRaiseCommand)) {
        throw CirrinaException.from("A timeout action must be a raise action");
      }

      // Acquire the name, which must be provided (otherwise it cannot be reset)
      final var actionName = timeoutActionObject.getName()
          .orElseThrow(() -> CirrinaException.from("A timeout action must have a name"));

      // Start the timeout task
      timeoutActionManager.start(actionName, (Number) delay, () -> {
        // Create span
        final var span = tracer.spanBuilder(SPAN_TIMEOUT)
            .startSpan();

        // Span attributes
        span.setAttribute(ATTR_ACTION_NAME, actionName);

        try (final var scope = span.makeCurrent()) {
          execute(List.of(actionTimeoutCommand), span);
        } catch (CirrinaException e) {
          logger.error("Failed to execute timeout command: {}", e.getMessage());
        } finally {
          span.end();
        }
      });
    }
  }

  /**
   * Stops a specific timeout action.
   *
   * @param actionName Name of timeout action to stop.
   * @throws CirrinaException In case of error.
   */
  private void stopTimeoutAction(String actionName) throws CirrinaException {
    timeoutActionManager.stop(actionName);
  }

  /**
   * Stops all currently started timeout actions.
   */
  private void stopAllTimeoutActions() {
    timeoutActionManager.stopAll();
  }

  /**
   * Switches the current active state to the provided state instance.
   *
   * @param state New active state.
   * @throws CirrinaException In case of error.
   */
  private void switchActiveState(State state) throws CirrinaException {
    if (!stateInstances.containsValue(state)) {
      throw CirrinaException.from(
          "A state with the name '%s' could not be found while attempting to set the new active state of state machine '%s'",
          state.getStateObject().getName(), stateMachineId);
    }

    // Update the active state
    activeState = state;
  }

  /**
   * Performs exiting a state.
   * <p>
   * The order of execution is as follows: stop all currently started timeout actions, cancel currently running while actions, execute exit
   * actions.
   *
   * @param exitingState StateClass instance that is being exited.
   * @param parentSpan   Parent span.
   * @throws CirrinaException In case of error.
   */
  private void doExit(State exitingState, Span parentSpan) throws CirrinaException {
    // Create span
    final var span = tracer.spanBuilder(SPAN_DO_EXIT)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    try (final var scope = span.makeCurrent()) {
      // Gather action commands
      final var exitActionCommands = exitingState.getExitActionCommands(
          stateScopedCommandFactory(exitingState, false));

      // Stop timeout actions
      stopAllTimeoutActions();

      // TODO: Cancel while actions

      // Execute in order
      execute(exitActionCommands, span);
    } finally {
      span.end();
    }
  }

  /**
   * Performs transitioning.
   * <p>
   * The order of execution is as follows: execute transition commands.
   *
   * @param transition TransitionClass instance that is being taken.
   * @param parentSpan Parent span.
   * @throws CirrinaException In case of error.
   */
  private void doTransition(Transition transition, Span parentSpan) throws CirrinaException {
    // Create span
    final var span = tracer.spanBuilder(SPAN_DO_TRANSITION)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    try (final var scope = span.makeCurrent()) {
      // Do not execute actions for else target transitions
      if (transition.isElse()) {
        return;
      }

      // Gather action commands
      final var transitionActionCommands = transition.getActionCommands(
          stateMachineScopedCommandFactory(this));

      // Execute in order
      execute(transitionActionCommands, span);
    } finally {
      span.end();
    }
  }

  /**
   * Performs entering a state.
   * <p>
   * The order of execution is as follows: execute entry actions, execute while actions, start timeout actions, switch to state.
   * <p>
   * Any subsequent always actions are selected at the end.
   *
   * @param enteringState StateClass instance that is being entered.
   * @param parentSpan    Parent span.
   * @throws CirrinaException In case of error.
   */
  private Optional<Transition> doEnter(State enteringState, Span parentSpan) throws CirrinaException {
    // Create span
    final var span = tracer.spanBuilder(SPAN_DO_ENTER)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    try (final var scope = span.makeCurrent()) {
      // Gather action commands
      final var entryActionCommands = enteringState.getEntryActionCommands(
          stateScopedCommandFactory(enteringState, false));

      final var whileActionCommands = enteringState.getWhileActionCommands(
          stateScopedCommandFactory(enteringState, true));

      final var timeoutActionObjects = enteringState.getTimeoutActionObjects();

      // Execute in order
      execute(entryActionCommands, span);
      execute(whileActionCommands, span);

      // Start timeout actions
      startAllTimeoutActions(timeoutActionObjects, span);

      // Switch the active state to the entering state
      switchActiveState(enteringState);

      return trySelectAlwaysTransition();
    } finally {
      span.end();
    }
  }

  /**
   * Handles an internal transition.
   *
   * @param transition TransitionClass instance.
   * @param parentSpan Parent span.
   * @throws CirrinaException In case of error.
   */
  private void handleInternalTransition(@NotNull Transition transition, Span parentSpan) throws CirrinaException {
    // Increment internal transitions counter
    meter.counterBuilder(METRIC_INTERNAL_TRANSITIONS).build().add(1);

    // Create span
    final var span = tracer.spanBuilder(SPAN_TRANSITION)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    // Span attributes
    span.setAttribute(ATTR_TRANSITION_TARGET_STATE_NAME, "");
    span.setAttribute(ATTR_TRANSITION_TYPE, "internal");

    try (final var scope = span.makeCurrent()) {
      // Only perform the transition
      doTransition(transition, span);
    } finally {
      span.end();
    }
  }

  /**
   * Handles an external transition.
   *
   * @param transition TransitionClass instance.
   * @param parentSpan Parent span.
   * @throws CirrinaException In case of error.
   */
  private void handleExternalTransition(@NotNull Transition transition, Span parentSpan) throws CirrinaException {
    // Increment external transitions counter
    meter.counterBuilder(METRIC_EXTERNAL_TRANSITIONS).build().add(1);

    // Create span
    final var span = tracer.spanBuilder(SPAN_TRANSITION)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    // Span attributes
    span.setAttribute(ATTR_TRANSITION_TARGET_STATE_NAME, transition.getTargetStateName());
    span.setAttribute(ATTR_TRANSITION_TYPE, "external");

    try (final var scope = span.makeCurrent()) {
      // Acquire the target state instance
      final var targetStateInstance = findStateInstanceByName(transition.getTargetStateName())
          .orElseThrow(() -> CirrinaException.from("Target state cannot be found in state machine"));

      // Exit the current state
      doExit(activeState, span);

      // Perform the transition
      doTransition(transition, span);

      // Enter the target state, if there is a follow-up transition, handle it recursively
      final var nextTransitionInstance = doEnter(targetStateInstance, span);

      if (nextTransitionInstance.isPresent()) {
        handleTransition(nextTransitionInstance.get(), span);
      }
    } finally {
      span.end();
    }
  }

  /**
   * Handles a transition.
   *
   * @param transition TransitionClass instance.
   * @param parentSpan Parent span.
   * @throws CirrinaException In case of error.
   */
  private void handleTransition(@NotNull Transition transition, Span parentSpan) throws CirrinaException {
    if (transition.isInternalTransition()) {
      handleInternalTransition(transition, parentSpan);
    } else {
      handleExternalTransition(transition, parentSpan);
    }
  }

  /**
   * Handles an event.
   * <p>
   * This function blocks until a new event is received.
   *
   * @param parentSpan Parent span.
   * @throws CirrinaException In case of error.
   */
  private Optional<Transition> handleEvent(Span parentSpan) throws InterruptedException, CirrinaException {
    // Wait for the next event, this call is blocking
    final var event = eventQueue.take();

    // Event attributes
    final var eventAttributes = Attributes.of(
        AttributeKey.stringKey(ATTR_EVENT_ID), event.getId(),
        AttributeKey.stringKey(ATTR_EVENT_NAME), event.getName(),
        AttributeKey.stringKey(ATTR_EVENT_CHANNEL), event.getChannel().toString()
    );

    parentSpan.addEvent(EVENT_RECEIVED_EVENT, eventAttributes);

    // Increment received events counter
    meter.counterBuilder(METRIC_RECEIVED_EVENTS).build().add(1);

    // Find a matching transition
    final var onTransition = trySelectOnTransition(event);

    // Set the event data
    onTransition.ifPresent(transition -> {
      // Increment handled events counter
      meter.counterBuilder(METRIC_HANDLED_EVENTS).build().add(1);

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
   * Runs this state machine instance.
   * <p>
   * Execution ends when the state machine instance has reached a terminal state or when it is interrupted.
   */
  @Override
  public void run() {
    // Create span
    final var span = tracer.spanBuilder(SPAN_RUN).startSpan();

    try (final var scope = span.makeCurrent()) {
      // Acquire the initial state instance
      final var initialStateInstance = stateInstances.get(stateMachineClassObject.getInitialState().getName());

      // TransitionClass into the initial state
      var nextTransition = doEnter(initialStateInstance, span);

      while (!isTerminated()) {
        // Wait for a next event, if no transition is selected. No transition is selected initially if the initial state has no selectable
        // always transition or thereafter if we've handled the selected transition
        if (nextTransition.isEmpty()) {
          nextTransition = handleEvent(span);
        }

        // If a transition is selected, handle it. The transition will be handled recursively; any transition selected due to entering a
        // next state is handled recursively. Therefore, if we're done handling this transition, we indicate that we need to wait for a new
        // event again
        if (nextTransition.isPresent()) {
          handleTransition(nextTransition.get(), span);

          nextTransition = Optional.empty();
        }
      }
    } catch (CirrinaException e) {
      logger.error("{} received a fatal error: {}", stateMachineId.toString(), e.getMessage());
    } catch (InterruptedException e) {
      logger.info("{} is interrupted", stateMachineId.toString());

      Thread.currentThread().interrupt();
    } finally {
      span.end();
    }

    logger.info("{} is done", stateMachineId.toString());
  }
}