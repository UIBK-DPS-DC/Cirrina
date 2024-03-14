package at.ac.uibk.dps.cirrina.core.runtime.statemachine;

import at.ac.uibk.dps.cirrina.core.runtime.event.Event;
import at.ac.uibk.dps.cirrina.core.runtime.event.EventListener;
import at.ac.uibk.dps.cirrina.core.runtime.state.State;
import java.util.UUID;

public class StateMachineExecutor implements EventListener {

  private final InstanceId instanceId = new InstanceId();

  private final StateMachineExecutor parentExecutor;

  private final Status status;

  public StateMachineExecutor(StateMachine stateMachine) {
    this(stateMachine, null);
  }

  public StateMachineExecutor(StateMachine stateMachine, StateMachineExecutor parentExecutor) throws RuntimeException {
    this.parentExecutor = parentExecutor;
    this.status = new Status(stateMachine.getInitialState());
  }

  private void step() {

  }

  @Override
  public void onReceiveEvent(Event event) {

  }

  static class Status {

    private State activateState;

    private boolean isAlive = true;

    public Status(State initialState) {
      this.activateState = initialState;
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
