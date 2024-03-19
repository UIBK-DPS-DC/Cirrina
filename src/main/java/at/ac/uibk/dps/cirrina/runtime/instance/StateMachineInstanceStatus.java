package at.ac.uibk.dps.cirrina.runtime.instance;

public class StateMachineInstanceStatus {

  private StateInstance activateState = null;

  private boolean isTerminated = false;

  public boolean isTerminated() {
    return isTerminated;
  }

  public StateInstance getActivateState() {
    return activateState;
  }

  public void setActivateState(StateInstance stateInstance) {
    activateState = stateInstance;
  }

  public void terminate() {
    this.isTerminated = true;
  }
}
