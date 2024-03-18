package at.ac.uibk.dps.cirrina.runtime.command;

import at.ac.uibk.dps.cirrina.object.action.Action;
import at.ac.uibk.dps.cirrina.runtime.StateMachineInstance;
import java.util.List;

public final class ActionCommand extends Command {

  private final Action action;

  private final boolean isWhile;

  public ActionCommand(StateMachineInstance stateMachineInstance, Action action, boolean isWhile) {
    super(stateMachineInstance);

    this.action = action;
    this.isWhile = isWhile;
  }

  @Override
  public List<Command> execute() {
    return null;
  }
}
