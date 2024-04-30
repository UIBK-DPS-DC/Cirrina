package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstanceId;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RuntimeScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SharedRuntime extends Runtime {

  public SharedRuntime(RuntimeScheduler runtimeScheduler, EventHandler eventHandler, Context persistentContext) throws CirrinaException {
    super(runtimeScheduler, eventHandler, persistentContext);
  }

  public List<StateMachineInstanceId> newInstance(CollaborativeStateMachine collaborativeStateMachine) throws CirrinaException {
    return newInstances(collaborativeStateMachine.getStateMachines(), Optional.empty());
  }

  private List<StateMachineInstanceId> newInstances(List<StateMachine> stateMachines, Optional<StateMachineInstanceId> parentInstanceId)
      throws CirrinaException {
    var instanceIds = new ArrayList<StateMachineInstanceId>();

    for (var stateMachine : stateMachines) {
      // Abstract state machines are skipped
      if (stateMachine.isAbstract()) {
        continue;
      }

      var instanceId = newInstance(stateMachine, parentInstanceId);
      instanceIds.add(instanceId);

      // Add nested state machines
      if (!stateMachine.getNestedStateMachines().isEmpty()) {
        instanceIds.addAll(newInstances(stateMachine.getNestedStateMachines(), Optional.of(instanceId)));
      }
    }

    return instanceIds;
  }
}
