package at.ac.uibk.dps.cirrina.runtime.command;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.action.Action;
import at.ac.uibk.dps.cirrina.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.object.action.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.runtime.StateMachineInstance;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ActionCommand extends Command {

  private static final Logger logger = LogManager.getLogger();

  private final Action action;

  private final boolean isWhile;

  public ActionCommand(StateMachineInstance stateMachineInstance, Action action, boolean isWhile) {
    super(stateMachineInstance);

    this.action = action;
    this.isWhile = isWhile;
  }

  private List<Command> execute(AssignAction action) throws RuntimeException {
    return null;
  }

  private List<Command> execute(CreateAction action) throws RuntimeException {
    return null;
  }

  private List<Command> execute(InvokeAction action) throws RuntimeException {
    return null;
  }

  private List<Command> execute(MatchAction action) throws RuntimeException {
    return null;
  }

  private List<Command> execute(RaiseAction action) throws RuntimeException {
    return null;
  }

  private List<Command> execute(TimeoutAction action) throws RuntimeException {
    return null;
  }

  private List<Command> execute(TimeoutResetAction action) throws RuntimeException {
    return null;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    logger.debug("Instance-{}: Execute action command", stateMachineInstance.instanceId);

    switch (action) {
      case AssignAction a -> {
        return execute(a);
      }
      case CreateAction a -> {
        return execute(a);
      }
      case InvokeAction a -> {
        return execute(a);
      }
      case MatchAction a -> {
        return execute(a);
      }
      case RaiseAction a -> {
        return execute(a);
      }
      case TimeoutAction a -> {
        return execute(a);
      }
      case TimeoutResetAction a -> {
        return execute(a);
      }
      default -> throw RuntimeException.from("Unknown action type in action command");
    }
  }
}
