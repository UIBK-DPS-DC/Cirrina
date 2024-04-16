package at.ac.uibk.dps.cirrina.runtime.service;

/**
 * HTTP service implementation, a service implementation that is accessible through HTTP.
 */
public class HttpServiceImplementation extends ServiceImplementation {

  private final String scheme;

  private final String host;

  private final int port;

  private final String endPoint;

  /**
   * Initializes this HTTP service implementation.
   *
   * @param parameters Initialization parameters.
   */
  public HttpServiceImplementation(Parameters parameters) {
    super(parameters.name, parameters.cost, parameters.local);

    this.scheme = parameters.scheme;
    this.host = parameters.host;
    this.port = parameters.port;
    this.endPoint = parameters.endPoint;
  }

  /**
   * Invoke this service implementation.
   */
  @Override
  public void invoke() {
    // TODO: Implement
  }

  /**
   * Returns the dynamic performance of this service implementation.
   *
   * @return Performance.
   */
  @Override
  public float getPerformance() {
    return 1.0f; // TODO: Implement measuring of performance
  }

  record Parameters(
      String name,
      float cost,
      boolean local,
      String scheme,
      String host,
      int port,
      String endPoint
  ) {

  }
}
