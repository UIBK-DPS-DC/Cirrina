package at.ac.uibk.dps.cirrina.runtime.command;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.context.Context;
import at.ac.uibk.dps.cirrina.object.event.EventHandler;
import java.util.List;

public interface Command {

  List<Command> execute() throws RuntimeException;

  interface Scope {

    List<Context> getExtent();

    EventHandler getEventHandler();
  }
}
