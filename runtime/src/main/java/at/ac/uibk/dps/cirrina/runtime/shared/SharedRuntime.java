package at.ac.uibk.dps.cirrina.runtime.shared;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.runtime.base.Runtime;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance.InstanceId;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RuntimeScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SharedRuntime extends Runtime {

  public SharedRuntime(RuntimeScheduler runtimeScheduler, EventHandler eventHandler, Context persistentContext) throws RuntimeException {
    super(runtimeScheduler, eventHandler, persistentContext);
  }

  public List<InstanceId> newInstance(CollaborativeStateMachine collaborativeStateMachine) throws RuntimeException {
    return newInstances(collaborativeStateMachine.getStateMachines(), Optional.empty());
  }

  private List<InstanceId> newInstances(List<StateMachine> stateMachines, Optional<InstanceId> parentInstanceId) throws RuntimeException {
    var instanceIds = new ArrayList<InstanceId>();

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
