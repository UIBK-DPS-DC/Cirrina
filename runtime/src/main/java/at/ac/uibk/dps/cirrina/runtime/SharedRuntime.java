package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstanceId;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class SharedRuntime extends Runtime {

  public SharedRuntime(EventHandler eventHandler, Context persistentContext) throws CirrinaException {
    super(eventHandler, persistentContext);
  }

  public List<StateMachineInstanceId> newInstance(
      CollaborativeStateMachine collaborativeStateMachine,
      ServiceImplementationSelector serviceImplementationSelector
  ) throws CirrinaException {
    return newInstances(collaborativeStateMachine.getStateMachines(), serviceImplementationSelector, null);
  }

  private List<StateMachineInstanceId> newInstances(
      List<StateMachine> stateMachines,
      ServiceImplementationSelector serviceImplementationSelector,
      @Nullable StateMachineInstanceId parentInstanceId
  ) throws CirrinaException {
    // TODO: This function needs to be replaced with a scripted instantiation. Instead of enumerating the SMs in the CSM, the script needs to instantiate specific SMs

    var instanceIds = new ArrayList<StateMachineInstanceId>();

    for (var stateMachine : stateMachines) {
      // Abstract state machines are skipped
      if (stateMachine.isAbstract()) {
        continue;
      }

      var instanceId = newInstance(stateMachine, serviceImplementationSelector, parentInstanceId);
      instanceIds.add(instanceId);

      // Add nested state machines
      if (!stateMachine.getNestedStateMachines().isEmpty()) {
        instanceIds.addAll(newInstances(stateMachine.getNestedStateMachines(), serviceImplementationSelector, instanceId));
      }
    }

    return instanceIds;
  }
}
