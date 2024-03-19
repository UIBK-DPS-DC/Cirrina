package at.ac.uibk.dps.cirrina.runtime.command.state;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import java.util.List;

public final class StateExitCommand implements Command {


  private StateInstance state;

  public StateExitCommand(StateInstance state) {
    this.state = state;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    return null;
  }
}
