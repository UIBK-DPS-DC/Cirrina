package at.ac.uibk.dps.cirrina.runtime.service;

import at.ac.uibk.dps.cirrina.runtime.service.HttpServiceImplementation.Parameters;
import at.ac.uibk.dps.cirrina.runtime.service.description.HttpServiceImplementationDescription;
import at.ac.uibk.dps.cirrina.runtime.service.description.ServiceImplementationDescription;

/**
 * Service implementation builder, builds service implementation objects.
 */
public class ServiceImplementationBuilder {

  private final ServiceImplementationDescription serviceImplementationDescription;

  private ServiceImplementationBuilder(ServiceImplementationDescription serviceImplementationDescription) {
    this.serviceImplementationDescription = serviceImplementationDescription;
  }

  /**
   * Construct a builder from a service implementation description.
   *
   * @param serviceImplementationDescription Service implementation description.
   * @return Builder.
   */
  public static ServiceImplementationBuilder from(ServiceImplementationDescription serviceImplementationDescription) {
    return new ServiceImplementationBuilder(serviceImplementationDescription);
  }

  /**
   * Builds the service implementation.
   *
   * @return Service implementation.
   */
  public ServiceImplementation build() {
    switch (serviceImplementationDescription) {
      case HttpServiceImplementationDescription s -> {
        return new HttpServiceImplementation(new Parameters(s.name, s.cost, s.local, s.scheme, s.host, s.port, s.endPoint, s.method));
      }
      default -> throw new IllegalStateException(String.format("Unexpected value: %s", serviceImplementationDescription.type));
    }
  }
}
