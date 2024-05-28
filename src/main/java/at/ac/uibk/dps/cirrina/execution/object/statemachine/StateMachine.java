package at.ac.uibk.dps.cirrina.execution.object.statemachine;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_PARENT_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_TRANSITION_STATE_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.EVENT_STATE_SWITCHED;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_ACTIONS_EXECUTED;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_ACTION_ASSIGN_LATENCY;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_ACTION_CREATE_LATENCY;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_ACTION_INVOKE_LATENCY;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_ACTION_RAISE_LATENCY;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_EVENTS_RECEIVED;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_TRANSITIONS_HANDLED;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.METRIC_TRANSITION_LATENCY;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_DO_ENTER;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_DO_EXIT;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_DO_TRANSITION;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_TIMEOUT;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_TRANSITION;

import at.ac.uibk.dps.cirrina.classes.state.StateClass;
import at.ac.uibk.dps.cirrina.classes.statemachine.StateMachineClass;
import at.ac.uibk.dps.cirrina.classes.transition.TransitionClass;
import at.ac.uibk.dps.cirrina.execution.command.ActionAssignCommand;
import at.ac.uibk.dps.cirrina.execution.command.ActionCommand;
import at.ac.uibk.dps.cirrina.execution.command.ActionCreateCommand;
import at.ac.uibk.dps.cirrina.execution.command.ActionInvokeCommand;
import at.ac.uibk.dps.cirrina.execution.command.ActionRaiseCommand;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import at.ac.uibk.dps.cirrina.execution.command.ExecutionContext;
import at.ac.uibk.dps.cirrina.execution.command.Scope;
import at.ac.uibk.dps.cirrina.execution.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.execution.object.context.Context;
import at.ac.uibk.dps.cirrina.execution.object.context.ContextBuilder;
import at.ac.uibk.dps.cirrina.execution.object.context.Extent;
import at.ac.uibk.dps.cirrina.execution.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.event.EventListener;
import at.ac.uibk.dps.cirrina.execution.object.state.State;
import at.ac.uibk.dps.cirrina.execution.object.transition.Transition;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.runtime.Runtime;
import at.ac.uibk.dps.cirrina.utils.Id;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

  private final Id stateMachineId = new Id();

  private final TimeoutActionManager timeoutActionManager = new TimeoutActionManager();

  private final BlockingQueue<Event> eventQueue = new LinkedBlockingDeque<>();

  private final Runtime parentRuntime;

  private final StateMachineClass stateMachineClass;

  private final ServiceImplementationSelector serviceImplementationSelector;

  private final @Nullable StateMachine parentStateMachine;

  private final Tracer tracer;

  private final Meter meter;

  private final StateMachineEventHandler stateMachineEventHandler;

  private final Context localContext;

  private final Map<String, State> stateInstances;

  private final Map<Class<? extends ActionCommand>, DoubleGauge> latencyGauges = new HashMap<>();

  private final DoubleGauge transitionLatencyGauge;

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
   * @param stateMachineClass             StateClass machine object
   * @param serviceImplementationSelector Service implementation selector.
   * @param parentStateMachine            Parent state machine instance or null.
   * @thread Runtime.
   */
  public StateMachine(
      Runtime parentRuntime,
      StateMachineClass stateMachineClass,
      ServiceImplementationSelector serviceImplementationSelector,
      OpenTelemetry openTelemetry,
      @Nullable StateMachine parentStateMachine
  ) {
    this.parentRuntime = parentRuntime;
    this.stateMachineClass = stateMachineClass;
    this.serviceImplementationSelector = serviceImplementationSelector;
    this.parentStateMachine = parentStateMachine;

    final var instrumentationScopeName = String.format("stateMachine-%s", stateMachineId.toString());

    // Create an OpenTelemetry tracer
    tracer = openTelemetry.getTracer(instrumentationScopeName);

    // Create an OpenTelemetry meter
    meter = openTelemetry.getMeter(instrumentationScopeName);

    stateMachineEventHandler = new StateMachineEventHandler(this, this.parentRuntime.getEventHandler());

    // Build the local context
    try {
      localContext = stateMachineClass.getLocalContextClass()
          .map(ContextBuilder::from)
          .orElseGet(ContextBuilder::from)
          .inMemoryContext()
          .build();
    } catch (IOException ignored) {
      throw new IllegalStateException(); // This should not happen
    }

    // Construct state instances
    stateInstances = stateMachineClass.vertexSet().stream()
        .collect(Collectors.toMap(StateClass::getName, state -> new State(state, this)));

    // Create latency counters
    latencyGauges.putAll(Map.of(
        ActionAssignCommand.class, meter.gaugeBuilder(METRIC_ACTION_ASSIGN_LATENCY).build(),
        ActionCreateCommand.class, meter.gaugeBuilder(METRIC_ACTION_CREATE_LATENCY).build(),
        ActionInvokeCommand.class, meter.gaugeBuilder(METRIC_ACTION_INVOKE_LATENCY).build(),
        ActionRaiseCommand.class, meter.gaugeBuilder(METRIC_ACTION_RAISE_LATENCY).build()
    ));

    // Create transition latency gauge
    transitionLatencyGauge = meter.gaugeBuilder(METRIC_TRANSITION_LATENCY).build();
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
        stateMachine,                  // Scope
        serviceImplementationSelector, // Service implementation selector
        stateMachineEventHandler,      // Event handler
        this,                          // Event listener
        false                          // Is while?
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
        state,                         // Scope
        serviceImplementationSelector, // Service implementation selector
        stateMachineEventHandler,      // Event handler
        this,                          // Event listener
        false                          // Is while?
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
   * @param event  Event to find an on transition for.
   * @param extent Extent.
   * @return On transition or empty in case no matching on transition can be selected.
   * @throws IllegalStateException If non-determinism is detected.
   */
  private Optional<Transition> trySelectOnTransition(Event event, Extent extent) throws IllegalStateException {
    // Find the transitions from the active state for the given event
    final var transitionObjects = stateMachineClass
        .findOnTransitionsFromStateByEventName(activeState.getStateObject(), event.getName());

    return trySelectTransition(transitionObjects, extent);
  }

  /**
   * Attempts to select an always transition.
   *
   * @param extent Extent.
   * @return Always transition or empty in case no always transition can be selected.
   * @throws IllegalStateException If non-determinism is detected.
   */
  private Optional<Transition> trySelectAlwaysTransition(Extent extent) throws IllegalStateException {
    // Find the transitions from the active state for the given event
    final var transitionObjects = stateMachineClass
        .findAlwaysTransitionsFromState(activeState.getStateObject());

    return trySelectTransition(transitionObjects, extent);
  }

  /**
   * Attempts to select a transition based on a collection of transition objects.
   * <p>
   * A transition is selected if it its guards evaluate to true, or if it does not, and it has an else target state.
   *
   * @param transitionObjects Collection of transition objects to select from.
   * @param extent            Extent.
   * @return Selected transition instance or empty in case no transition can be selected.
   * @throws IllegalStateException If non-determinism is detected.
   * @throws IllegalStateException If no transition could be selected.
   */
  private Optional<Transition> trySelectTransition(List<? extends TransitionClass> transitionObjects, Extent extent)
      throws IllegalStateException {
    try {
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
        default -> throw new IllegalStateException("Non-determinism detected");
      };
    } catch (UnsupportedOperationException e) {
      throw new IllegalStateException("No transition could be selected", e);
    }
  }

  /**
   * Executes this collection of commands provided.
   * <p>
   * Commands are executed recursively. Any command that is the result of a command execution is executed immediately following the command
   * that created it.
   *
   * @param actionCommands Commands to execute.
   * @param parentSpan     Parent span.
   * @throws UnsupportedOperationException If the action commands cannot be executed.
   */
  private void execute(List<ActionCommand> actionCommands, Span parentSpan) throws UnsupportedOperationException {
    try {
      for (final var actionCommand : actionCommands) {
        // Increment executed actions counter
        meter.counterBuilder(METRIC_ACTIONS_EXECUTED)
            .setDescription("The total number of actions executed.")
            .build()
            .add(1, getAttributes());

        // Attempt to get the latency gauge
        final var latencyGauge = latencyGauges.getOrDefault(actionCommand.getClass(), null);

        // Execute and acquire new commands
        final var newCommands = actionCommand.execute(tracer, parentSpan, latencyGauge);

        // Execute any subsequent command
        execute(newCommands, parentSpan);
      }
    } catch (UnsupportedOperationException e) {
      throw new UnsupportedOperationException("Could not execute action commands", e);
    }
  }

  /**
   * Starts all timeout actions as provided.
   * <p>
   * Timeout actions are executed until stopped.
   *
   * @param timeoutActionObjects Timeout action objects to start.
   * @param parentSpan           Parent span.
   * @throws UnsupportedOperationException If the delay expression does not evaluate to a numeric value.
   * @throws IllegalArgumentException      If the timeout action is not a raise action.
   * @throws IllegalArgumentException      If the timeout action does not have a name.
   */
  private void startAllTimeoutActions(
      List<TimeoutAction> timeoutActionObjects, Span parentSpan
  ) throws UnsupportedOperationException, IllegalArgumentException {
    for (final var timeoutActionObject : timeoutActionObjects) {
      // The evaluated delay value is required to be numeric
      final var delay = timeoutActionObject.getDelay().execute(getExtent());

      if (!(delay instanceof Number)) {
        throw new UnsupportedOperationException(
            "The delay expression '%s' did not evaluate to a numeric value".formatted(timeoutActionObject.getDelay()));
      }

      // Create action command
      final var actionTimeoutCommand = stateMachineScopedCommandFactory(this).createActionCommand(timeoutActionObject.getAction());

      if (!(actionTimeoutCommand instanceof ActionRaiseCommand)) {
        throw new IllegalArgumentException("A timeout action must be a raise action");
      }

      // Acquire the name, which must be provided (otherwise it cannot be reset)
      final var actionName = timeoutActionObject.getName()
          .orElseThrow(() -> new IllegalArgumentException("A timeout action must have a name"));

      // Start the timeout task
      timeoutActionManager.start(actionName, (Number) delay, () -> {
        // Create span
        final var span = tracer.spanBuilder(SPAN_TIMEOUT)
            .startSpan();

        // Span attributes
        span.setAllAttributes(getAttributes());
        span.setAllAttributes(actionTimeoutCommand.getAttributes());

        try (final var scope = span.makeCurrent()) {
          execute(List.of(actionTimeoutCommand), span);
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
   * @throws IllegalArgumentException If not exactly one timeout action was found with the provided name.
   */
  private void stopTimeoutAction(String actionName) throws IllegalArgumentException {
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
   * @param state      New active state.
   * @param parentSpan Parent span.
   * @throws IllegalArgumentException If the state is not known.
   */
  private void switchActiveState(State state, Span parentSpan) throws IllegalArgumentException {
    final var stateName = state.getStateObject().getName();

    if (!stateInstances.containsValue(state)) {
      throw new IllegalArgumentException("A state '%s' does not exist".formatted(stateName));
    }

    // Event attributes
    final var eventAttributes = Attributes.of(
        AttributeKey.stringKey(ATTR_TRANSITION_STATE_NAME), stateName
    );

    parentSpan.addEvent(EVENT_STATE_SWITCHED, eventAttributes);

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
   * @throws UnsupportedOperationException If the exit action cannot be executed.
   */
  private void doExit(State exitingState, Span parentSpan) throws UnsupportedOperationException {
    // Create span
    final var span = tracer.spanBuilder(SPAN_DO_EXIT)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    // Span attributes
    span.setAllAttributes(getAttributes());

    try (final var scope = span.makeCurrent()) {
      // Gather action commands
      final var exitActionCommands = exitingState.getExitActionCommands(
          stateScopedCommandFactory(exitingState, false));

      // Stop timeout actions
      stopAllTimeoutActions();

      // TODO: Cancel while actions

      // Execute in order
      try {
        execute(exitActionCommands, span);
      } catch (UnsupportedOperationException e) {
        throw new UnsupportedOperationException("Could not execute exit actions", e);
      }
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
   * @throws UnsupportedOperationException If the transition action cannot be executed.
   */
  private void doTransition(Transition transition, Span parentSpan) throws UnsupportedOperationException {
    // Create span
    final var span = tracer.spanBuilder(SPAN_DO_TRANSITION)
        .setParent(io.opentelemetry.context.Context.current().with(parentSpan))
        .startSpan();

    // Span attributes
    span.setAllAttributes(getAttributes());

    try (final var scope = span.makeCurrent()) {
      // Do not execute actions for else target transitions
      if (transition.isElse()) {
        return;
      }

      // Gather action commands
      final var transitionActionCommands = transition.getActionCommands(
          stateMachineScopedCommandFactory(this));

      // Execute in order
      try {
        execute(transitionActionCommands, span);
      } catch (UnsupportedOperationException e) {
        throw new UnsupportedOperationException("Could not execute transition actions", e);
      }
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
   * @param parentSpan    Parent span or null.
   * @throws UnsupportedOperationException If the entry/while actions could not be executed.
   * @throws UnsupportedOperationException If the timeout actions could not be started.
   * @throws UnsupportedOperationException If the states could not be switched.
   * @throws UnsupportedOperationException If an always transition could not be selected.
   */
  private Optional<Transition> doEnter(
      State enteringState,
      @Nullable Span parentSpan
  ) throws UnsupportedOperationException, IllegalArgumentException {
    // Create span
    final var spanBuilder = tracer.spanBuilder(SPAN_DO_ENTER)
        .setParent(
            parentSpan != null ? io.opentelemetry.context.Context.current().with(parentSpan) : io.opentelemetry.context.Context.root());

    final var span = spanBuilder.startSpan();

    // Span attributes
    span.setAllAttributes(getAttributes());

    try (final var scope = span.makeCurrent()) {
      // Gather action commands
      final var entryActionCommands = enteringState.getEntryActionCommands(
          stateScopedCommandFactory(enteringState, false));

      final var whileActionCommands = enteringState.getWhileActionCommands(
          stateScopedCommandFactory(enteringState, true));

      final var timeoutActionObjects = enteringState.getTimeoutActionObjects();

      // Execute in order
      try {
        execute(entryActionCommands, span);
        execute(whileActionCommands, span);
      } catch (UnsupportedOperationException e) {
        throw new UnsupportedOperationException("Could not execute entry/while actions", e);
      }

      // Start timeout actions
      try {
        startAllTimeoutActions(timeoutActionObjects, span);
      } catch (UnsupportedOperationException | IllegalArgumentException e) {
        throw new UnsupportedOperationException("Could not start timeout actions", e);
      }

      // Switch the active state to the entering state
      try {
        switchActiveState(enteringState, span);
      } catch (IllegalArgumentException e) {
        throw new UnsupportedOperationException("Could not switch states");
      }

      try {
        return trySelectAlwaysTransition(getExtent());
      } catch (IllegalStateException e) {
        throw new UnsupportedOperationException("Could not select always transition");
      }
    } finally {
      span.end();
    }
  }

  /**
   * Handles an internal transition.
   *
   * @param transition TransitionClass instance.
   * @throws UnsupportedOperationException If the transition could not be handled.
   */
  private void handleInternalTransition(@NotNull Transition transition) throws UnsupportedOperationException {
    // Create span
    final var span = tracer.spanBuilder(SPAN_TRANSITION)
        .startSpan();

    // Span attributes
    span.setAllAttributes(getAttributes());
    span.setAllAttributes(transition.getAttributes());

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
   * @throws UnsupportedOperationException If the transition could not be handled.
   */
  private void handleExternalTransition(@NotNull Transition transition) throws UnsupportedOperationException {
    // Create span
    final var span = tracer.spanBuilder(SPAN_TRANSITION)
        .startSpan();

    final var targetStateName = transition.getTargetStateName().get();

    // Span attributes
    span.setAllAttributes(getAttributes());
    span.setAllAttributes(transition.getAttributes());

    try (final var scope = span.makeCurrent()) {
      // Acquire the target state instance
      final var targetStateInstance = findStateInstanceByName(targetStateName)
          .orElseThrow(() -> new IllegalArgumentException(
              "Target state '%s' cannot be found in state machine".formatted(transition.getTargetStateName())));

      // Exit the current state
      doExit(activeState, span);

      // Perform the transition
      doTransition(transition, span);

      // Enter the target state, if there is a follow-up transition, handle it recursively
      final var nextTransitionInstance = doEnter(targetStateInstance, span);

      nextTransitionInstance.ifPresent(this::handleTransition);
    } finally {
      span.end();
    }
  }

  /**
   * Handles a transition.
   *
   * @param transition TransitionClass instance.
   * @throws UnsupportedOperationException If the transition could not be handled.
   */
  private void handleTransition(@NotNull Transition transition) throws UnsupportedOperationException {
    // Increment transitions counter
    meter.counterBuilder(METRIC_TRANSITIONS_HANDLED)
        .setDescription("The total number of transitions handled.")
        .build()
        .add(1, getAttributes());

    final var a = System.nanoTime() / 1.0e6;

    if (transition.isInternalTransition()) {
      handleInternalTransition(transition);
    } else {
      handleExternalTransition(transition);
    }

    // Transition handling latency
    transitionLatencyGauge.set(System.nanoTime() / 1.0e6 - a);
  }

  /**
   * Handles an event.
   * <p>
   * This function blocks until a new event is received.
   *
   * @throws InterruptedException          If interrupted while waiting for an event.
   * @throws UnsupportedOperationException If an on transition could not be selected.
   */
  private Optional<Transition> handleEvent() throws InterruptedException, UnsupportedOperationException {
    // Wait for the next event, this call is blocking
    final var event = eventQueue.take();

    // Increment handled events counter
    meter.counterBuilder(METRIC_EVENTS_RECEIVED)
        .setDescription("The total number of events handled.")
        .build()
        .add(1, getAttributes());

    // Find a matching transition
    try {
      // Create a temporary in-memory context containing the event data
      final var eventDataContext = new InMemoryContext();

      for (var contextVariable : event.getData()) {
        eventDataContext.create(EVENT_DATA_VARIABLE_PREFIX + contextVariable.name(), contextVariable.value());
      }

      // Create a temporary extent that contains the event data
      final var extent = getExtent().extend(eventDataContext);

      final var onTransition = trySelectOnTransition(event, extent);

      // Set the event data in the actual extent
      onTransition.ifPresent(transition -> {
        try {
          for (var contextVariable : event.getData()) {
            getExtent().setOrCreate(EVENT_DATA_VARIABLE_PREFIX + contextVariable.name(), contextVariable.value());
          }
        } catch (IOException e) {
          logger.error("Failed to set event data", e);
        }
      });

      return onTransition;
    } catch (IOException | IllegalStateException e) {
      throw new UnsupportedOperationException("Could not select on transition", e);
    }
  }

  /**
   * Runs this state machine instance.
   * <p>
   * Execution ends when the state machine instance has reached a terminal state or when it is interrupted.
   */
  @Override
  public void run() {
    try {
      // Acquire the initial state instance
      final var initialStateInstance = stateInstances.get(stateMachineClass.getInitialState().getName());

      // TransitionClass into the initial state
      var nextTransition = doEnter(initialStateInstance, null);

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
    } catch (InterruptedException e) {
      logger.info("{} is interrupted", stateMachineId.toString());

      Thread.currentThread().interrupt();
    } catch (Exception e) {
      logger.error("%s received a fatal error".formatted(stateMachineId.toString()), e);
    }

    logger.info("{} is done", stateMachineId.toString());
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
  public Id getStateMachineInstanceId() {
    return stateMachineId;
  }

  /**
   * Returns the state machine object.
   *
   * @return StateClass machine object.
   */
  public StateMachineClass getStateMachineClass() {
    return stateMachineClass;
  }

  /**
   * Get OpenTelemetry attributes of this state machine.
   *
   * @return Attributes.
   */
  public Attributes getAttributes() {
    return Attributes.of(
        AttributeKey.stringKey(ATTR_STATE_MACHINE_ID), stateMachineId.toString(),
        AttributeKey.stringKey(ATTR_STATE_MACHINE_NAME), stateMachineClass.getName(),
        AttributeKey.stringKey(ATTR_STATE_MACHINE_PARENT_ID), Optional.ofNullable(parentStateMachine)
            .map(stateMachine -> stateMachine.getStateMachineInstanceId().toString())
            .orElse("")
    );
  }
}
