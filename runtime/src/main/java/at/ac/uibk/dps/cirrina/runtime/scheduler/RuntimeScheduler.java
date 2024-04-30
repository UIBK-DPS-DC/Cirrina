package at.ac.uibk.dps.cirrina.runtime.scheduler;

import at.ac.uibk.dps.cirrina.execution.command.Command;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import java.util.List;

public interface RuntimeScheduler {

  List<StateMachineInstanceCommand> select(List<StateMachineInstance> instances);

  record StateMachineInstanceCommand(StateMachineInstance stateMachineInstance, Command command) {

  }
}
