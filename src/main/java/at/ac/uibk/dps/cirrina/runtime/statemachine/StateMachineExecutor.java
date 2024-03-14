package at.ac.uibk.dps.cirrina.runtime.statemachine;

import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.context.Context;
import at.ac.uibk.dps.cirrina.runtime.event.Event;
import at.ac.uibk.dps.cirrina.runtime.event.EventListener;
import at.ac.uibk.dps.cirrina.runtime.state.State;
import java.util.UUID;

public class StateMachineExecutor implements EventListener {

  private final InstanceId instanceId = new InstanceId();

  private final StateMachineExecutor parentExecutor;

  private final Status status;

  public StateMachineExecutor(StateMachine stateMachine, Context localContext) {
    this(stateMachine, null, localContext);
  }

  public StateMachineExecutor(StateMachine stateMachine, StateMachineExecutor parentExecutor, Context localContext)
      throws RuntimeException {
    this.parentExecutor = parentExecutor;
    this.status = new Status(stateMachine.getInitialState(), localContext);
  }

  private void execute(Command command) {

  }

  private void step() {

  }

  @Override
  public void onReceiveEvent(Event event) {

  }

  static class Status {

    private State activateState;

    private Context localContext;

    private boolean isAlive = true;

    public Status(State initialState, Context localContext) {
      this.activateState = initialState;
      this.localContext = localContext;
    }

    public void setAlive(boolean isAlive) {
      this.isAlive = isAlive;
    }
  }

  static class InstanceId {

    private final UUID uuid = UUID.randomUUID();

    @Override
    public String toString() {
      return uuid.toString();
    }
  }
}
