package at.ac.uibk.dps.cirrina.execution.service;

import at.ac.uibk.dps.cirrina.execution.service.HttpServiceImplementation.Parameters;
import at.ac.uibk.dps.cirrina.execution.service.description.HttpServiceImplementationDescription;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationDescription;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Service implementation builder, builds service implementation objects.
 */
public class ServiceImplementationBuilder {

  private final ServiceImplementationDescription[] serviceImplementationDescriptions;

  private ServiceImplementationBuilder(ServiceImplementationDescription[] serviceImplementationDescriptions) {
    this.serviceImplementationDescriptions = serviceImplementationDescriptions;
  }

  /**
   * Construct a builder from a service implementation description.
   *
   * @param serviceImplementationDescription Service implementation description.
   * @return Builder.
   */
  public static ServiceImplementationBuilder from(ServiceImplementationDescription serviceImplementationDescription) {
    return new ServiceImplementationBuilder(new ServiceImplementationDescription[]{serviceImplementationDescription});
  }

  /**
   * Construct a builder from multiple service implementation descriptions.
   *
   * @param serviceImplementationDescriptions Service implementation descriptions.
   * @return Builder.
   */
  public static ServiceImplementationBuilder from(ServiceImplementationDescription[] serviceImplementationDescriptions) {
    return new ServiceImplementationBuilder(serviceImplementationDescriptions);
  }

  /**
   * Builds the service implementation.
   *
   * @return Service implementation.
   */
  private static ServiceImplementation buildOne(ServiceImplementationDescription serviceImplementationDescription) {
    switch (serviceImplementationDescription) {
      case HttpServiceImplementationDescription s -> {
        return new HttpServiceImplementation(new Parameters(s.name, s.cost, s.local, s.scheme, s.host, s.port, s.endPoint, s.method));
      }
      default -> throw new IllegalStateException(String.format("Unexpected value: %s", serviceImplementationDescription.type));
    }
  }

  /**
   * Builds the service implementations.
   *
   * @return Service implementations.
   */
  public Multimap<String, ServiceImplementation> build() {
    Multimap<String, ServiceImplementation> services = ArrayListMultimap.create();

    for (var serviceDescription : serviceImplementationDescriptions) {
      var service = buildOne(serviceDescription);

      services.put(service.getName(), service);
    }

    return services;
  }
}
