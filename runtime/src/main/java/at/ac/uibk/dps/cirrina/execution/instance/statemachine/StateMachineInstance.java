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
import at.ac.uibk.dps.cirrina.execution.command.Command;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import at.ac.uibk.dps.cirrina.execution.command.ExecutionContext;
import at.ac.uibk.dps.cirrina.execution.command.Scope;
import at.ac.uibk.dps.cirrina.execution.instance.state.StateInstance;
import at.ac.uibk.dps.cirrina.runtime.Runtime;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class StateMachineInstance implements EventListener, Scope, CommandQueueAdapter {

  public static final String EVENT_DATA_VARIABLE = "event_data";

  private static final Logger logger = LogManager.getLogger();

  private final StateMachineInstanceId stateMachineInstanceId = new StateMachineInstanceId();

  private final Semaphore queueSemaphore = new Semaphore(1);

  private final Deque<Command> commandQueue = new LinkedList<>();

  private final TimeoutActionManager timeoutActionManager = new TimeoutActionManager();

  private final Status status = new Status();

  private final Runtime parentRuntime;

  private final StateMachine stateMachineObject;

  private final StateMachineInstance parentStateMachineInstance;

  private final StateMachineInstanceEventHandler stateMachineInstanceEventHandler;

  private final Context localContext;

  private final Map<String, StateInstance> stateInstances;

  /**
   * Initializes this state machine instance object. A state machine instance is associated with a state machine object that describes its
   * static structure.
   * <p>
   * A state machine instance is parented in a runtime.
   * <p>
   * Additionally, a state machine instance may be parented in a state machine instance, forming a nested state machine instance.
   *
   * @param parentRuntime              Parent runtime.
   * @param stateMachineObject         State machine object
   * @param parentStateMachineInstance Parent state machine instance or null.
   * @throws CirrinaException In case of error.
   * @thread Runtime.
   */
  public StateMachineInstance(
      Runtime parentRuntime,
      StateMachine stateMachineObject,
      @Nullable StateMachineInstance parentStateMachineInstance
  ) throws CirrinaException {
    this.parentRuntime = parentRuntime;
    this.stateMachineObject = stateMachineObject;
    this.parentStateMachineInstance = parentStateMachineInstance;

    this.stateMachineInstanceEventHandler = new StateMachineInstanceEventHandler(this, this.parentRuntime.getEventHandler());

    // Build the local context
    this.localContext = stateMachineObject.getLocalContextClass()
        .map(ContextBuilder::from)
        .orElseGet(ContextBuilder::from)
        .inMemoryContext()
        .build();

    // Create the event data variable
    localContext.create(EVENT_DATA_VARIABLE, "");

    // Construct state instances
    this.stateInstances = stateMachineObject.vertexSet().stream()
        .collect(Collectors.toMap(State::getName, state -> new StateInstance(state, this)));

    // Enter an enter state command that enters the initial state
    final var initialStateInstance = stateInstances.get(stateMachineObject.getInitialState().getName());

    final var commandFactory = new CommandFactory(buildExecutionContext());

    final var transitionInitialCommand = commandFactory.createTransitionInitialCommand(initialStateInstance);

    addCommandsToFront(List.of(transitionInitialCommand));
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
    if (status.isTerminated()) {
      return;
    }

    final var commandFactory = new CommandFactory(buildExecutionContext());

    final var eventcommand = commandFactory.createEventCommand(event);

    // Append a new event command, this command may do nothing depending on the event
    try {
      //queueSemaphore.acquire();

      addCommandsToBack(List.of(eventcommand));
    } /*catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }*/ finally {
      //queueSemaphore.release();
    }
  }

  public Optional<List<Command>> takeNextCommand() {
    if (!queueSemaphore.tryAcquire()) {
      return Optional.empty();
    }

    if (commandQueue.isEmpty()) {
      queueSemaphore.release();
      return Optional.empty();
    }

    return Optional.of(List.of(commandQueue.pop()));
  }

  /**
   * Add a collection of commands to the back of the command queue.
   * <p>
   * This function is thread-safe on the execution thread, and otherwise needs to be guarded by the queue semaphore.
   *
   * @param commandList Commands to add to the queue.
   * @thread Mixed.
   */
  @Override
  public void addCommandsToBack(List<Command> commandList) {
    // Add all commands to the back of the queue
    commandQueue.addAll(commandList);
  }

  /**
   * Add a collection of commands to the front of the command queue.
   * <p>
   * This function is thread-safe on the execution thread, and otherwise needs to be guarded by the queue semaphore.
   *
   * @param commandList Commands to add to the queue.
   * @thread Mixed.
   */
  @Override
  public void addCommandsToFront(List<Command> commandList) {
    // Add all commands to the front of the queue
    Collections.reverse(commandList);
    commandList.forEach(commandQueue::addFirst);
  }

  /**
   * Execute a command on this state machine instance.
   *
   * @param command The command to execute.
   * @thread Execution.
   */
  public void execute(Command command) {
    try {
      // The semaphore is required to be acquired at this point and this function is responsible for releasing it again
      assert (queueSemaphore.availablePermits() == 0);

      // Execute the command
      command.execute();
    } catch (CirrinaException e) {
      logger.error("State machine instance error while executing command: {}", e.getMessage());
    } finally {
      // Release the semaphore
      queueSemaphore.release();
    }
  }

  /**
   * Starts the provided timeout action.
   *
   * @param action Timeout action.
   * @throws CirrinaException If an error occurred.
   * @thread Execution.
   */
  public void startTimeoutAction(TimeoutAction action) throws CirrinaException {
    // Acquire the delay value
    final var delayExpression = action.getDelay();

    final var extent = getExtent();

    // The evaluated delay value is required to be numeric
    final var delay = delayExpression.execute(extent);

    if (!(delay instanceof Number)) {
      throw CirrinaException.from("The delay expression '%s' did not evaluate to a numeric value", delayExpression);
    }

    final var commandFactory = new CommandFactory(buildExecutionContext());

    final var actionTimeoutCommand = commandFactory.createActionCommand(action);

    // Schedule the timeout action periodically
    final Runnable timeoutTask = () -> addCommandsToBack(List.of(actionTimeoutCommand));

    // Acquire the name, which must be provided (otherwise it cannot be reset)
    final var actionName = action.getName().orElseThrow(() -> CirrinaException.from("A timeout action must have a name"));

    // Start the timeout task
    timeoutActionManager.start(actionName, (Number) delay, timeoutTask);
  }

  /**
   * Stops a timeout action with the provided name.
   *
   * @param actionName Name of action to stop.
   * @throws CirrinaException In case not exactly one timeout action was found with the provided name.
   * @thread Execution.
   */
  public void stopTimeoutAction(String actionName) throws CirrinaException {
    timeoutActionManager.stop(actionName);
  }

  /**
   * Stops all timeout actions.
   *
   * @thread Execution.
   */
  public void stopAllTimeoutActions() {
    timeoutActionManager.stopAll();
  }

  /**
   * Updates the current state.
   *
   * @param stateInstance Name of the new current state.
   * @throws CirrinaException If a state with that name is not known.
   * @thread Execution.
   */
  public void updateActiveState(StateInstance stateInstance) throws CirrinaException {
    if (!stateInstances.containsValue(stateInstance)) {
      throw CirrinaException.from(
          "A state with the name '%s' could not be found while attempting to set the new active state of state machine '%s'",
          stateInstance.getState().getName(), stateMachineInstanceId);
    }

    // Update the active state
    status.setActiveState(stateInstance);
  }

  /**
   * Attempts to find a state instance by its name. If none is found, empty is returned.
   *
   * @param name Name to find.
   * @return State instance or empty.
   * @thread Execution.
   */
  public Optional<StateInstance> findStateInstanceByName(String name) {
    return stateInstances.containsKey(name) ? Optional.of(stateInstances.get(name)) : Optional.empty();
  }

  private ExecutionContext buildExecutionContext() {
    return new ExecutionContext(this, this, status, stateMachineInstanceEventHandler, this, false);
  }

  @Override
  public Extent getExtent() {
    return Optional.ofNullable(parentStateMachineInstance)
        .map(parent -> parent.getExtent().extend(localContext))
        .orElseGet(() -> parentRuntime.getExtent().extend(localContext));
  }

  public StateMachineInstanceId getStateMachineInstanceId() {
    return stateMachineInstanceId;
  }

  public StateMachine getStateMachineObject() {
    return stateMachineObject;
  }
}
