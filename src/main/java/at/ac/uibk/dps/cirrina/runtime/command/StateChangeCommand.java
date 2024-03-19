package at.ac.uibk.dps.cirrina.runtime.command;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.runtime.StateMachineInstance;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class StateChangeCommand extends Command {

  private static final Logger logger = LogManager.getLogger();

  private State state;

  public StateChangeCommand(StateMachineInstance stateMachineInstance, State state) {
    super(stateMachineInstance);

    this.state = state;
  }

  @Override
  public List<Command> execute() throws RuntimeException {
    logger.debug("Instance-{}: Execute state change command", stateMachineInstance.instanceId);

    // Switch states
    stateMachineInstance.setActiveStateByName(state.getName());

    return null;
  }
}
