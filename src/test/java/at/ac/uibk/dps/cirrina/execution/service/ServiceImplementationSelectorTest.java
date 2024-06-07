package at.ac.uibk.dps.cirrina.execution.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import at.ac.uibk.dps.cirrina.execution.service.description.HttpServiceImplementationDescription;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationDescription;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationType;
import org.junit.jupiter.api.Test;

public class ServiceImplementationSelectorTest {

  @Test
  public void testSelectMatchingServices() {
    final var serviceDescriptions = new ServiceImplementationDescription[5];

    // Service one
    {
      final var service = new HttpServiceImplementationDescription();
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
      final var service = new HttpServiceImplementationDescription();
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
      final var service = new HttpServiceImplementationDescription();
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
      final var service = new HttpServiceImplementationDescription();
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
      final var service = new HttpServiceImplementationDescription();
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

    final var services = ServiceImplementationBuilder.from(serviceDescriptions).build();

    final var serviceSelector = new OptimalServiceImplementationSelector(services);

    assertDoesNotThrow(() -> {
      var selected = serviceSelector.select("A", false);

      assertEquals(0.5f, selected.get().getCost(), 0.0001);
    });

    // TODO: Add additional tests
  }
}
