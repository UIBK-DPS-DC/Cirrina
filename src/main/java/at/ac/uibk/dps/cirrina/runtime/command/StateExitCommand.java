package at.ac.uibk.dps.cirrina.runtime.command;

import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.runtime.StateMachineInstance;
import java.util.List;

public final class StateExitCommand extends Command {

  private State state;

  public StateExitCommand(StateMachineInstance stateMachineInstance, State state) {
    super(stateMachineInstance);

    this.state = state;
  }

  @Override
  public List<Command> execute() {
    return null;
  }
}
