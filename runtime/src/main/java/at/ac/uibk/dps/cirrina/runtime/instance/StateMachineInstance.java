package at.ac.uibk.dps.cirrina.runtime.instance;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.ContextBuilder;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.core.object.event.EventListener;
import at.ac.uibk.dps.cirrina.core.object.state.State;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.runtime.base.Runtime;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.Command.ExecutionContext;
import at.ac.uibk.dps.cirrina.runtime.command.Command.Scope;
import at.ac.uibk.dps.cirrina.runtime.command.action.TimeoutActionCommand;
import at.ac.uibk.dps.cirrina.runtime.command.event.EventCommand;
import at.ac.uibk.dps.cirrina.runtime.command.transition.InitialTransitionCommand;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class StateMachineInstance implements Scope, EventListener {

  /**
   * Name of the variable in the local context that contains (temporary) event data.
   */
  public static final String EVENT_DATA_VARIABLE = "event_data";

  /**
   * The instance ID of the state machine instance.
   */
  private final InstanceId instanceId = new InstanceId();

  /**
   * Execution lock.
   */
  private final ReentrantLock executionLock = new ReentrantLock();

  /**
   * Command queue.
   */
  private final Deque<Command> commandQueue = new ConcurrentLinkedDeque<>();

  /**
   * Timeout task scheduler.
   */
  private final ScheduledExecutorService timeoutScheduler = Executors.newScheduledThreadPool(1);

  /**
   * Timeout tasks.
   */
  private final ConcurrentMap<TimeoutAction, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();

  /**
   * The state machine instance's status.
   */
  private final Status status = new Status();

  /**
   * The containing runtime.
   */
  private final Runtime runtime;

  /**
   * The state machine object.
   */
  private final StateMachine stateMachineObject;


  /**
   * The parent state machine instance, can be empty.
   */
  private final Optional<StateMachineInstance> parent;


  /**
   * The local context.
   * <p>
   * TODO: Can be moved to status.
   */
  private final Context localContext;

  /**
   * Collection of state instances.
   */
  private final Map<String, StateInstance> stateInstances;

  /**
   * Initializes this state machine instance.
   * <p>
   * The state machine instance is associated with a containing runtime that manages its execution. Multiple instances can be associated
   * with one object. The object represents the static state machine, and an instance will not modify the object.
   * <p>
   * A state machine instance can be parented in another state machine instance. In this case, the resulting nested state machine is
   * executed in parallel and associated with the same runtime.
   *
   * @param runtime            The containing runtime.
   * @param stateMachineObject The state machine object.
   * @param parent             The parent state machine, can be empty in case this is a top-level state machine instance.
   * @throws CirrinaException In case of an error while initializing.
   */
  public StateMachineInstance(Runtime runtime, StateMachine stateMachineObject, Optional<StateMachineInstance> parent)
      throws CirrinaException {
    this.runtime = runtime;
    this.stateMachineObject = stateMachineObject;
    this.parent = parent;

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
    appendCommand(new InitialTransitionCommand(stateInstances.get(stateMachineObject.getInitialState().getName())));
  }

  /**
   * On event received handler.
   *
   * @param event Event received.
   * @thread Mixed.
   */
  @Override
  public void onReceiveEvent(Event event) {
    // Nothing to do if the state machine is terminated
    if (status.isTerminated()) {
      return;
    }

    // Append a new event command, this command may do nothing depending on the event
    appendCommand(new EventCommand(this, event));
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

    final var delay = delayExpression.execute(getExtent());
    if (!(delay instanceof Number)) {
      throw CirrinaException.from("The delay expression '%s' did not evaluate to a numeric value", delayExpression);
    }

    // Schedule the timeout action periodically
    final Runnable timeoutTask = () -> appendCommand(new TimeoutActionCommand(this, action, false));
    final var future = timeoutScheduler.scheduleWithFixedDelay(timeoutTask, 0, ((Number) delay).intValue(), TimeUnit.MILLISECONDS);
    timeoutTasks.put(action, future);
  }

  /**
   * Stops a timeout action with the provided name.
   *
   * @param actionName Name of action to stop.
   * @throws CirrinaException In case not exactly one timeout action was found with the provided name.
   * @thread Execution.
   */
  public void stopTimeoutAction(String actionName) throws CirrinaException {
    boolean found = false;

    // Find and remove the timeout action
    for (final var it = timeoutTasks.entrySet().iterator(); it.hasNext(); ) {
      final var entry = it.next();

      final var name = entry.getKey().getName().orElseThrow(() -> CirrinaException.from("A timeout action name cannot be empty"));
      final var timeoutTask = entry.getValue();

      if (name.equals(actionName)) {
        // Just to me sure, this should not happen
        if (found) {
          throw CirrinaException.from("Found more than one timeout action with the name '%s'", actionName);
        }

        // Cancel the task
        timeoutTask.cancel(true);

        // And remove it
        it.remove();

        found = true;
      }

      // We require one timeout action to be found
      if (!found) {
        throw CirrinaException.from("Could not stop a timeout action with the name '%s'", actionName);
      }
    }

    // A timeout action must have a name
    timeoutTasks.entrySet().stream()
        .filter(entry -> entry.getKey().getName().orElse(null).equals(actionName))
        .forEach(entry -> {
          timeoutTasks.remove(entry.getKey());
        });
  }

  /**
   * Stops all timeout actions.
   *
   * @thread Execution.
   */
  public void stopAllTimeoutActions() {
    timeoutTasks.values().forEach(future -> future.cancel(true));
    timeoutTasks.clear();
  }

  /**
   * Executes a command, the state machine instance must be locked through a call to getExecutableCommand(). This state machine instance
   * will be unlocked after the execution of the command.
   *
   * @param command The command to execute.
   * @thread Execution.
   * @see StateMachineInstance#findExecutableCommand().
   */
  public void execute(Command command) throws CirrinaException {
    // When executing a command, this state machine instance must be locked, as the command should only be acquired along with a lock. This
    // must always hold, otherwise we made a programming error
    assert executionLock.isLocked();

    if (!status.isTerminated()) {
      // When executing a command, new commands replacing the currently executed command can be generated, insert those commands into the
      // command queue at the beginning. The currently executing command is always the head of the queue, hence we can insert these commands
      // as the new head
      prependCommands(command.execute(new ExecutionContext(this, runtime.getEventHandler())));

      // If we're in a terminal state, the state machine also terminates
      if (status.getActivateState().getState().isTerminal()) {
        status.terminate();
      }
    }

    // Unlock the state machine instance
    executionLock.unlock();
  }

  /**
   * Prepends the list of commands to the queue.
   *
   * @param commands List of commands to prepend.
   * @thread Execution.
   */
  private void prependCommands(List<Command> commands) {
    Collections.reverse(commands);
    commands.forEach(commandQueue::addFirst);

    // Wake up the runtime, a new command is available
    synchronized (runtime) {
      runtime.notify();
    }
  }

  /**
   * Add a command to this state machine's queue. Adding a command will wake up the runtime.
   *
   * @param command The command to add to the queue.
   */
  private void appendCommand(Command command) {
    // Add the command to the queue
    commandQueue.add(command);

    // Wake up the runtime, a new command is available
    synchronized (runtime) {
      runtime.notify();
    }
  }

  /**
   * Returns an executable command whenever it is available. An executable command is available whenever this state machine has commands in
   * its queue and a lock can be acquired (i.e., the state machine instance is not locked).
   * <p>
   * In case a command is returned, the state machine instance will be locked and will need to be unlocked once the command has been
   * executed. It is, therefore, expected that the command once acquired is executed.
   *
   * @return The next executable command.
   */
  public Optional<Command> findExecutableCommand() {
    if (!commandQueue.isEmpty()) {
      if (executionLock.tryLock()) {
        return Optional.ofNullable(commandQueue.poll());
      }
    }

    return Optional.empty();
  }

  /**
   * Attempts to find a state instance by its name. If none is found, empty is returned.
   *
   * @param name Name to find.
   * @return State instance or empty.
   */
  public Optional<StateInstance> findStateInstanceByName(String name) {
    return stateInstances.containsKey(name) ? Optional.of(stateInstances.get(name)) : Optional.empty();
  }

  /**
   * Returns the extent.
   *
   * @return Extent.
   */
  @Override
  public Extent getExtent() {
    return parent
        .map(parent -> parent.getExtent().extend(localContext))
        .orElseGet(() -> runtime.getExtent().extend(localContext));
  }

  /**
   * Returns the instance ID.
   *
   * @return Instance ID.
   */
  public InstanceId getInstanceId() {
    return instanceId;
  }

  /**
   * Returns the status.
   *
   * @return Status.
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Returns the state machine object.
   *
   * @return State machine object.
   */
  public StateMachine getStateMachineObject() {
    return stateMachineObject;
  }

  public void setActiveState(StateInstance state) throws CirrinaException {
    if (!stateInstances.containsValue(state)) {
      throw CirrinaException.from(
          "A state with the name '%s' could not be found while attempting to set the new active state of state machine '%s'",
          state.getState().getName(), instanceId);
    }

    // Update the active state
    status.activeState = state;
  }


  public static class InstanceId {

    private final UUID uuid = UUID.randomUUID();

    @Override
    public String toString() {
      return uuid.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      var instanceId = (InstanceId) o;
      return Objects.equals(uuid, instanceId.uuid);
    }

    @Override
    public int hashCode() {
      return Objects.hash(uuid);
    }
  }

  /**
   * TODO: Check thread-safety.
   */
  public static class Status {

    private StateInstance activeState = null;

    private boolean isTerminated = false;

    public boolean isTerminated() {
      return isTerminated;
    }

    public StateInstance getActivateState() {
      return activeState;
    }

    public void terminate() {
      this.isTerminated = true;
    }
  }
}
