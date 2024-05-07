package at.ac.uibk.dps.cirrina.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.lang.parser.Parser;
import at.ac.uibk.dps.cirrina.core.lang.parser.Parser.Options;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.execution.service.ServicesImplementationBuilder;
import at.ac.uibk.dps.cirrina.execution.service.description.HttpServiceImplementationDescription;
import at.ac.uibk.dps.cirrina.execution.service.description.HttpServiceImplementationDescription.Method;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationDescription;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationType;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationsDescription;
import at.ac.uibk.dps.cirrina.runtime.data.DefaultDescriptions;
import com.google.common.collect.ArrayListMultimap;
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
import org.junit.jupiter.api.RepeatedTest;

public class ServiceInvocationTest {

  private static CollaborativeStateMachine collaborativeStateMachine;

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

    final var parser = new Parser(new Options());
    Assertions.assertDoesNotThrow(() -> {
      collaborativeStateMachine = CollaborativeStateMachineBuilder.from(parser.parse(json)).build();
    });
  }

  @AfterAll
  public static void tearDown() {
    httpServer.stop(0);
  }

  @RepeatedTest(10)
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

          // And should count up to 10
          //assertTrue((Integer) value <= 10);

          super.assign(name, value);
        }
      };

      // Create the persistent context variables v and e
      mockPersistentContext.create("v", 0);
      mockPersistentContext.create("e", 0);

      // Create a runtime
      final var runtime = new SharedRuntime(mockEventHandler, mockPersistentContext);

      // Create a service implementation description
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

      // Create a service implementation selector
      final var serviceImplementationDescription = ArrayListMultimap.create();

      servicesDescription.serviceImplementations = serviceDescriptions;

      final var services = ServicesImplementationBuilder.from(servicesDescription).build();

      final var serviceImplementationSelector = new ServiceImplementationSelector(services);

      // Create a new collaborative state machine instance
      final var instances = runtime.newInstance(collaborativeStateMachine, serviceImplementationSelector);

      assertEquals(1, instances.size());

      final var instance = runtime.findInstance(instances.getFirst()).get();

      runtime.shutdown(5000);

      assertEquals(10, mockPersistentContext.get("v"));
    });
  }
}
