package at.ac.uibk.dps.cirrina.runtime.instance;

import at.ac.uibk.dps.cirrina.object.context.Context;
import at.ac.uibk.dps.cirrina.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.runtime.command.Command.Scope;
import java.util.List;
import java.util.stream.Stream;

public class StateInstance implements Scope {

  private final Context localContext = new InMemoryContext();

  private final State state;

  private final StateMachineInstance parent;

  public StateInstance(State state, StateMachineInstance parent) {
    this.state = state;
    this.parent = parent;
  }

  @Override
  public List<Context> getExtent() {
    return Stream.concat(
            parent.getExtent().stream(),
            Stream.of(localContext))
        .toList();
  }

  @Override
  public EventHandler getEventHandler() {
    return null;
  }

  public State getState() {
    return state;
  }
}
