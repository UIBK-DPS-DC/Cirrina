package at.ac.uibk.dps.cirrina.runtime.service;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariable;
import java.util.List;

/**
 * Abstract service implementation, needs to be specialized.
 *
 * @see HttpServiceImplementation
 */
public abstract class ServiceImplementation {

  private final String name;
  private final float cost;
  private final boolean local;

  /**
   * Initializes this service implementation.
   *
   * @param name  Name of this service implementation.
   * @param cost  Cost of this service implementation.
   * @param local Whether this service implementation is local.
   */
  public ServiceImplementation(String name, float cost, boolean local) {
    this.name = name;
    this.cost = cost;
    this.local = local;
  }

  /**
   * Invoke this service implementation.
   *
   * @param input Input to the service invocation.
   * @return The service invocation output.
   * @throws RuntimeException If the service invocation failed.
   */
  public abstract List<ContextVariable> invoke(List<ContextVariable> input) throws RuntimeException;


  /**
   * Returns the dynamic performance of this service implementation.
   *
   * @return Performance.
   */
  public abstract float getPerformance();

  /**
   * Returns whether this service implementation is local.
   *
   * @return Is local.
   */
  public boolean isLocal() {
    return local;
  }

  /**
   * Returns the name of this service implementation.
   *
   * @return Name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the cost of this service implementation.
   *
   * @return Cost.
   */
  public float getCost() {
    return cost;
  }
}
