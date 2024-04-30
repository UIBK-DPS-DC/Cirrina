package at.ac.uibk.dps.cirrina.execution.instance.state;

import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.state.State;
import at.ac.uibk.dps.cirrina.execution.command.Scope;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;

public final class StateInstance implements Scope {

  private final Context localContext = new InMemoryContext();

  private final State state;

  private final StateMachineInstance parent;

  public StateInstance(State state, StateMachineInstance parent) {
    this.state = state;
    this.parent = parent;
  }

  @Override
  public Extent getExtent() {
    return parent.getExtent().extend(localContext);
  }


  public State getState() {
    return state;
  }
}
