package at.ac.uibk.dps.cirrina.runtime.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import at.ac.uibk.dps.cirrina.runtime.service.description.HttpServiceImplementationDescription;
import at.ac.uibk.dps.cirrina.runtime.service.description.ServiceImplementationDescription;
import at.ac.uibk.dps.cirrina.runtime.service.description.ServiceImplementationType;
import at.ac.uibk.dps.cirrina.runtime.service.description.ServiceImplementationsDescription;
import org.junit.jupiter.api.Test;

public class ServiceImplementationSelectorTest {

  @Test
  public void testSelectMatchingServices() {
    var servicesDescription = new ServiceImplementationsDescription();

    var serviceDescriptions = new ServiceImplementationDescription[5];

    // Service one
    {
      var service = new HttpServiceImplementationDescription();
      service.name = "A";
      service.type = ServiceImplementationType.HTTP;
      service.cost = 1.0f;
      service.local = true;
      service.scheme = "http";
      service.host = "localhost";
      service.port = 12345;
      service.endPoint = "";

      serviceDescriptions[0] = service;
    }

    // Service two
    {
      var service = new HttpServiceImplementationDescription();
      service.name = "A";
      service.type = ServiceImplementationType.HTTP;
      service.cost = 0.5f;
      service.local = false;
      service.scheme = "http";
      service.host = "localhost";
      service.port = 12345;
      service.endPoint = "";

      serviceDescriptions[1] = service;
    }

    // Service three
    {
      var service = new HttpServiceImplementationDescription();
      service.name = "B";
      service.type = ServiceImplementationType.HTTP;
      service.cost = 0.4f;
      service.local = false;
      service.scheme = "http";
      service.host = "localhost";
      service.port = 12345;
      service.endPoint = "";

      serviceDescriptions[2] = service;
    }

    // Service four
    {
      var service = new HttpServiceImplementationDescription();
      service.name = "B";
      service.type = ServiceImplementationType.HTTP;
      service.cost = 0.2f;
      service.local = false;
      service.scheme = "http";
      service.host = "localhost";
      service.port = 12345;
      service.endPoint = "";

      serviceDescriptions[3] = service;
    }

    // Service five
    {
      var service = new HttpServiceImplementationDescription();
      service.name = "C";
      service.type = ServiceImplementationType.HTTP;
      service.cost = 1.0f;
      service.local = true;
      service.scheme = "http";
      service.host = "localhost";
      service.port = 12345;
      service.endPoint = "";

      serviceDescriptions[4] = service;
    }

    servicesDescription.serviceImplementations = serviceDescriptions;

    var services = ServicesImplementationBuilder.from(servicesDescription).build();

    var serviceSelector = new ServiceImplementationSelector(services);

    assertDoesNotThrow(() -> {
      var selected = serviceSelector.select("A", false);

      assertEquals(0.5f, selected.get().getCost(), 0.0001);
    });

    // TODO: Add additional tests
  }
}
