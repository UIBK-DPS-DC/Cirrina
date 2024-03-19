package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.context.Context;
import at.ac.uibk.dps.cirrina.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.StateEntryCommand;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;

public class StateMachineInstance {

  public final InstanceId instanceId = new InstanceId();

  private final ReentrantLock lock = new ReentrantLock();

  private final Deque<Command> queue = new ConcurrentLinkedDeque<>();

  private final Runtime runtime;

  private final StateMachine stateMachine;

  private final StateMachineInstance parent;

  private final Status status;

  public StateMachineInstance(Runtime runtime, StateMachine stateMachine) throws RuntimeException {
    this(runtime, stateMachine, null);
  }

  public StateMachineInstance(Runtime runtime, StateMachine stateMachine, StateMachineInstance parent) throws RuntimeException {
    this.runtime = runtime;
    this.stateMachine = stateMachine;
    this.parent = parent;
    this.status = new Status(null);

    // Enter an enter state command that enters the initial state
    appendCommand(new StateEntryCommand(this, stateMachine.getInitialState()));
  }

  /**
   * Sets the active state by state name.
   *
   * @param stateName Name of the new active state.
   * @throws RuntimeException If the state name is not a valid state within this state machine.
   */
  public void setActiveStateByName(String stateName) throws RuntimeException {
    status.activateState = stateMachine.findStateByName(stateName).orElseThrow(() -> RuntimeException.from(
        "A state with the name '%s' could not be found while attempting to set the new active state of state machine '%s'",
        stateName, instanceId));
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
  public Optional<Command> getExecutableCommand() {
    if (!queue.isEmpty()) {
      if (lock.tryLock()) {
        return Optional.ofNullable(queue.poll());
      }
    }

    return Optional.empty();
  }

  /**
   * Executes a command, the state machine instance must be locked through a call to getExecutableCommand(). This state machine instance
   * will be unlocked after the execution of the command.
   *
   * @param command The command to execute.
   * @see StateMachineInstance#getExecutableCommand().
   */
  void execute(Command command) throws RuntimeException {
    // When executing a command, this state machine instance must be locked, as the command should only be acquired along with a lock. This
    // must always hold, otherwise we made a programming error
    assert lock.isLocked();

    // When executing a command, new commands replacing the currently executed command can be generated, insert those commands into the
    // command queue at the beginning. The currently executing command is always the head of the queue, hence we can insert these commands
    // as the new head
    prependCommands(command.execute());

    // Unlock the state machine instance
    lock.unlock();
  }

  private void prependCommands(List<Command> commands) {
    Collections.reverse(commands);
    commands.stream()
        .forEach(queue::addFirst);

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
    queue.add(command);

    // Wake up the runtime, a new command is available
    synchronized (runtime) {
      runtime.notify();
    }
  }

  static class Status {

    private final Context localContext = new InMemoryContext();

    private State activateState;

    private boolean isAlive = true;

    public Status(State initialState) {
      this.activateState = initialState;
    }

    public void setAlive(boolean isAlive) {
      this.isAlive = isAlive;
    }
  }

  public static class InstanceId {

    private final UUID uuid = UUID.randomUUID();

    @Override
    public String toString() {
      return uuid.toString();
    }
  }
}
