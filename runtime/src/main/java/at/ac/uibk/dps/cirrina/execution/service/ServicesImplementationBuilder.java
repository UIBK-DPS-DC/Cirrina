package at.ac.uibk.dps.cirrina.execution.service;

import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationsDescription;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Services implementation builder, builds service implementations objects.
 */
public class ServicesImplementationBuilder {

  private final ServiceImplementationsDescription serviceImplementationsDescription;

  private ServicesImplementationBuilder(ServiceImplementationsDescription serviceImplementationsDescription) {
    this.serviceImplementationsDescription = serviceImplementationsDescription;
  }

  /**
   * Construct a builder from a service implementations description.
   *
   * @param serviceImplementationsDescription Service implementations description.
   * @return Builder.
   */
  public static ServicesImplementationBuilder from(ServiceImplementationsDescription serviceImplementationsDescription) {
    return new ServicesImplementationBuilder(serviceImplementationsDescription);
  }

  /**
   * Builds the service implementations.
   *
   * @return Service implementations.
   */
  public Multimap<String, ServiceImplementation> build() {
    Multimap<String, ServiceImplementation> services = ArrayListMultimap.create();

    for (var serviceDescription : serviceImplementationsDescription.serviceImplementations) {
      var service = ServiceImplementationBuilder.from(serviceDescription).build();

      services.put(service.getName(), service);
    }

    return services;
  }
}
