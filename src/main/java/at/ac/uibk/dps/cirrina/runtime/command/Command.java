package at.ac.uibk.dps.cirrina.runtime.command;

import at.ac.uibk.dps.cirrina.runtime.StateMachineInstance;
import java.util.List;

public abstract class Command {

  protected final StateMachineInstance stateMachineInstance;

  public Command(StateMachineInstance stateMachineInstance) {
    this.stateMachineInstance = stateMachineInstance;
  }

  public abstract List<Command> execute();
}
