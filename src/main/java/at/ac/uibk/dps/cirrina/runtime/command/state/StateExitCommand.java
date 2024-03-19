package at.ac.uibk.dps.cirrina.runtime.command.state;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import java.util.List;

public final class StateExitCommand implements Command {


  private State state;

  public StateExitCommand(Scope scope, State state) {
    this.state = state;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    return null;
  }
}
