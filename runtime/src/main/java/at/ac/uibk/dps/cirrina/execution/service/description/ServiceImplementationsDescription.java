package at.ac.uibk.dps.cirrina.execution.service.description;

import jakarta.validation.constraints.NotNull;

/**
 * Service implementations description, represents a collection of service implementation descriptions.
 */
public class ServiceImplementationsDescription {

  /**
   * Collection of service implementation descriptions.
   */
  @NotNull
  public ServiceImplementationDescription[] serviceImplementations;
}
