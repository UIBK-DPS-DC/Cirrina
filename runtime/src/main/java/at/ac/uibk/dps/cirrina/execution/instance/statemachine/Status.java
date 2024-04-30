package at.ac.uibk.dps.cirrina.execution.instance.statemachine;

import at.ac.uibk.dps.cirrina.execution.instance.state.StateInstance;

public final class Status {

  private StateInstance activeState = null;

  public boolean isTerminated() {
    return false;
  }

  public StateInstance getActivateState() {
    return activeState;
  }

  public void setActiveState(StateInstance activeState) {
    this.activeState = activeState;
  }
}
