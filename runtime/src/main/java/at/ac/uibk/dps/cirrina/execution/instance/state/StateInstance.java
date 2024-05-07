package at.ac.uibk.dps.cirrina.execution.instance.state;

import at.ac.uibk.dps.cirrina.core.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.state.State;
import at.ac.uibk.dps.cirrina.execution.command.ActionCommand;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import at.ac.uibk.dps.cirrina.execution.command.Scope;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class StateInstance implements Scope {

  private final Context localContext = new InMemoryContext();

  private final State stateObject;

  private final StateMachineInstance parent;

  public StateInstance(State stateObject, StateMachineInstance parent) {
    this.stateObject = stateObject;
    this.parent = parent;
  }

  @Override
  public Extent getExtent() {
    return parent.getExtent().extend(localContext);
  }

  public State getStateObject() {
    return stateObject;
  }

  public List<ActionCommand> getEntryActionCommands(CommandFactory commandFactory) {
    List<ActionCommand> actionCommands = new ArrayList<>();

    new TopologicalOrderIterator<>(stateObject.getEntryActionGraph()).forEachRemaining(
        action -> actionCommands.add(commandFactory.createActionCommand(action)));

    return actionCommands;
  }

  public List<ActionCommand> getWhileActionCommands(CommandFactory commandFactory) {
    List<ActionCommand> actionCommands = new ArrayList<>();

    new TopologicalOrderIterator<>(stateObject.getWhileActionGraph()).forEachRemaining(
        action -> actionCommands.add(commandFactory.createActionCommand(action)));

    return actionCommands;
  }

  public List<ActionCommand> getExitActionCommands(CommandFactory commandFactory) {
    List<ActionCommand> actionCommands = new ArrayList<>();

    new TopologicalOrderIterator<>(stateObject.getExitActionGraph()).forEachRemaining(
        action -> actionCommands.add(commandFactory.createActionCommand(action)));

    return actionCommands;
  }

  public List<TimeoutAction> getTimeoutActionObjects() {
    List<TimeoutAction> timeoutActionObjects = new ArrayList<>();

    new TopologicalOrderIterator<>(stateObject.getAfterActionGraph()).forEachRemaining(
        timeoutActionObject -> timeoutActionObjects.add((TimeoutAction) timeoutActionObject));

    return timeoutActionObjects;
  }
}
