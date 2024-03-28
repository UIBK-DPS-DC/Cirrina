package at.ac.uibk.dps.cirrina.runtime.command;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import java.util.List;

/**
 * Command, represents the interface to commands that can be entered into a state machine's command queue. Commands can be executed and may
 * produce new commands and have side effects.
 */
public interface Command {

  List<Command> execute(ExecutionContext executionContext) throws RuntimeException;

  interface Scope {

    Extent getExtent();
  }

  record ExecutionContext(
      StateMachineInstance stateMachineInstance,
      EventHandler eventHandler
  ) {

  }
}
