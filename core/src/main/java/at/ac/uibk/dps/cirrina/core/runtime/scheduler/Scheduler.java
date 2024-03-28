package at.ac.uibk.dps.cirrina.core.runtime.scheduler;

import at.ac.uibk.dps.cirrina.core.runtime.command.Command;
import at.ac.uibk.dps.cirrina.core.runtime.instance.StateMachineInstance;
import java.util.Optional;
import java.util.Queue;

public interface Scheduler {

  Optional<StateMachineInstanceCommand> select(Queue<StateMachineInstance> instances);

  record StateMachineInstanceCommand(StateMachineInstance instance, Command command) {

  }
}
