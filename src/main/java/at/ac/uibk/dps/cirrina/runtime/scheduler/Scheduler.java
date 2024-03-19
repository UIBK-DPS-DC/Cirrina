package at.ac.uibk.dps.cirrina.runtime.scheduler;

import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import java.util.Optional;
import java.util.Queue;

public interface Scheduler {

  Optional<StateMachineInstanceCommand> select(Queue<StateMachineInstance> instances);

  record StateMachineInstanceCommand(StateMachineInstance instance, Command command) {

  }
}
