package at.ac.uibk.dps.cirrina.execution.runtime.offline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClass;
import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClassBuilder;
import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription;
import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.execution.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.execution.service.ServicesImplementationBuilder;
import at.ac.uibk.dps.cirrina.execution.service.description.HttpServiceImplementationDescription;
import at.ac.uibk.dps.cirrina.execution.service.description.HttpServiceImplementationDescription.Method;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationDescription;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationType;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationsDescription;
import at.ac.uibk.dps.cirrina.io.description.DescriptionParser;
import at.ac.uibk.dps.cirrina.runtime.OfflineRuntime;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.fury.Fury;
import io.fury.config.Language;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ServiceInvocationTest {

  private static CollaborativeStateMachineClass collaborativeStateMachineClass;

  private static HttpServer httpServer;

  @BeforeAll
  public static void setUp() throws IOException {
    httpServer = HttpServer.create(new InetSocketAddress(8000), 0);

    httpServer.createContext("/increment", new HttpHandler() {
      public void handle(HttpExchange exchange) throws IOException {
        final var payload = exchange.getRequestBody().readAllBytes();

        final var fury = Fury.builder()
            .withLanguage(Language.XLANG)
            .requireClassRegistration(false)
            .build();

        // Deserialize the payload
        final var in = fury.deserialize(payload);

        assertInstanceOf(Map.class, in);

        // Check variables
        assertTrue(((Map<?, ?>) in).containsKey("v"));

        // Require integers
        assertInstanceOf(Integer.class, ((Map<?, ?>) in).get("v"));

        // Create output
        final var out = fury.serialize(Map.of(
            "v",
            (int) ((Map<?, ?>) in).get("v") + 1));

        // Response stateMachineInstanceStatus and length
        exchange.sendResponseHeaders(200, out.length);

        // Output the response
        try (final var stream = exchange.getResponseBody()) {
          stream.write(out);
        }
      }
    });

    httpServer.start();

    final var json = DefaultDescriptions.invoke;

    final var parser = new DescriptionParser<CollaborativeStateMachineDescription>(CollaborativeStateMachineDescription.class);
    Assertions.assertDoesNotThrow(() -> {
      collaborativeStateMachineClass = CollaborativeStateMachineClassBuilder.from(parser.parse(json)).build();
    });
  }

  @AfterAll
  public static void tearDown() {
    httpServer.stop(0);
  }

  @Test
  public void testServiceInvocationExecute() {
    Assertions.assertDoesNotThrow(() -> {
      final var mockEventHandler = new EventHandler() {

        @Override
        public void close() throws Exception {

        }

        @Override
        public void sendEvent(Event event, String source) {
          propagateEvent(event);
        }

        @Override
        public void subscribe(String topic) {

        }

        @Override
        public void unsubscribe(String topic) {

        }

        @Override
        public void subscribe(String source, String subject) {

        }

        @Override
        public void unsubscribe(String source, String subject) {

        }
      };

      // Mock a persistent context using an in-memory context
      var mockPersistentContext = new InMemoryContext() {
        @Override
        public void assign(String name, Object value) throws CirrinaException {
          // Don't expect any variables assigned except for v
          assertTrue(name.equals("v") || name.equals("e"));

          // Which is an integer
          assertInstanceOf(Integer.class, value);

          super.assign(name, value);
        }
      };

      // Create the persistent context variables v and e
      mockPersistentContext.create("v", 0);
      mockPersistentContext.create("e", 0);

      final var runtime = new OfflineRuntime(mockEventHandler, mockPersistentContext);

      var servicesDescription = new ServiceImplementationsDescription();

      var serviceDescriptions = new ServiceImplementationDescription[1];

      {
        var service = new HttpServiceImplementationDescription();
        service.name = "increment";
        service.type = ServiceImplementationType.HTTP;
        service.cost = 1.0f;
        service.local = true;
        service.scheme = "http";
        service.host = "localhost";
        service.port = 8000;
        service.endPoint = "/increment";
        service.method = Method.GET;

        serviceDescriptions[0] = service;
      }

      servicesDescription.serviceImplementations = serviceDescriptions;

      final var services = ServicesImplementationBuilder.from(servicesDescription).build();
      final var serviceImplementationSelector = new ServiceImplementationSelector(services);

      final var instances = runtime.newInstance(collaborativeStateMachineClass, serviceImplementationSelector);

      assertEquals(1, instances.size());

      final var instance = runtime.findInstance(instances.getFirst()).get();

      assertTrue(runtime.waitForCompletion(10000));

      assertEquals(10, mockPersistentContext.get("v"));
    });
  }
}
