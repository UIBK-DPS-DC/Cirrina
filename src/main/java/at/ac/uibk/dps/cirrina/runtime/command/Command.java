package at.ac.uibk.dps.cirrina.runtime.command;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.context.Extent;
import at.ac.uibk.dps.cirrina.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import java.util.List;

public interface Command {

  List<Command> execute(ExecutionContext executionContext) throws RuntimeException;

  interface Scope {

    Extent getExtent();

    EventHandler getEventHandler();
  }

  record ExecutionContext(
      StateMachineInstance stateMachineInstance,
      EventHandler eventHandler
  ) {

  }
}
