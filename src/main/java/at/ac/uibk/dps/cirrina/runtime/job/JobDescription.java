package at.ac.uibk.dps.cirrina.runtime.job;

import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationDescription;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Job description, describes a job as published to a collection of runtime systems.
 */
public class JobDescription {

  /**
   * Service implementations description, containing the service implementations provided to a runtime for the instantiation of a state
   * machine.
   */
  @NotNull
  public ServiceImplementationDescription[] serviceImplementations;

  /**
   * The collaborative state machine containing the state machine to instantiate.
   */
  @NotNull
  public CollaborativeStateMachineDescription collaborativeStateMachine;

  /**
   * Then name of the state machine to instantiate, as containing within the collaborative state machine.
   */
  @NotNull
  public String stateMachineName;

  /**
   * The collection of data to inject into the state machine instance's local context.
   */
  @NotNull
  public Map<String, String> localData;

  /**
   * The collection of IDs of state machine instances to bind to.
   */
  @NotNull
  public List<String> bindEventInstanceIds;
}
