package at.ac.uibk.dps.cirrina.runtime.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.context.ContextVariableClass;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariableBuilder;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.execution.service.HttpServiceImplementation;
import at.ac.uibk.dps.cirrina.execution.service.HttpServiceImplementation.Parameters;
import at.ac.uibk.dps.cirrina.execution.service.description.HttpServiceImplementationDescription.Method;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.fury.Fury;
import io.fury.config.Language;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HttpServiceImplementationTest {

  private static HttpServer httpServer;

  @BeforeAll
  public static void setUp() throws IOException {
    httpServer = HttpServer.create(new InetSocketAddress(8000), 0);

    httpServer.createContext("/plus", new HttpHandler() {
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
        assertTrue(((Map<?, ?>) in).containsKey("varOne"));
        assertTrue(((Map<?, ?>) in).containsKey("varTwo"));

        // Require integers
        assertInstanceOf(Integer.class, ((Map<?, ?>) in).get("varOne"));
        assertInstanceOf(Integer.class, ((Map<?, ?>) in).get("varTwo"));

        // Create output
        final var out = fury.serialize(Map.of(
            "result",
            (int) ((Map<?, ?>) in).get("varOne") + (int) ((Map<?, ?>) in).get("varTwo")));

        // Response status and length
        exchange.sendResponseHeaders(200, out.length);

        // Output the response
        try (final var stream = exchange.getResponseBody()) {
          stream.write(out);
        }
      }
    });

    httpServer.createContext("/error", new HttpHandler() {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
        // Response status and length
        exchange.sendResponseHeaders(500, 0);

        // Output the response
        try (final var stream = exchange.getResponseBody()) {
          stream.write(new byte[0]);
        }
      }
    });

    httpServer.createContext("/broken-response1", new HttpHandler() {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
        // Response status and length
        exchange.sendResponseHeaders(200, 1);

        // Output the response
        try (final var stream = exchange.getResponseBody()) {
          stream.write(new byte[]{1});
        }
      }
    });

    httpServer.createContext("/broken-response2", new HttpHandler() {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
        final var fury = Fury.builder()
            .withLanguage(Language.XLANG)
            .requireClassRegistration(false)
            .build();

        // Create output
        final var out = fury.serialize(Map.of(1, 2));

        // Response status and length
        exchange.sendResponseHeaders(200, out.length);

        // Output the response
        try (final var stream = exchange.getResponseBody()) {
          stream.write(out);
        }
      }
    });

    httpServer.start();
  }

  @AfterAll
  public static void tearDown() {
    httpServer.stop(0);
  }

  @Test
  public void testHttpServiceInvocation() throws IOException {
    List.of(Method.POST, Method.GET).stream()
        .forEach(method -> {
          // First variable
          final var varOne = new ContextVariableClass();
          varOne.name = "varOne";
          varOne.value = new ExpressionClass("5");

          // Second variable
          final var varTwo = new ContextVariableClass();
          varTwo.name = "varTwo";
          varTwo.value = new ExpressionClass("6");

          // Success
          assertDoesNotThrow(() -> {
            final var variables = new ArrayList<ContextVariable>();
            variables.add(ContextVariableBuilder.from(varOne).build().evaluate(new Extent()));
            variables.add(ContextVariableBuilder.from(varTwo).build().evaluate(new Extent()));

            final var service = new HttpServiceImplementation(new Parameters(
                "http",
                1.0f,
                false,
                "http",
                "localhost",
                8000,
                "/plus",
                Method.POST));

            final var output = service.invoke(variables).get();

            assertEquals(output.size(), 1);

            final var result = output.getFirst();
            assertEquals(result.name(), "result");
            assertEquals(result.value(), 11);
          });

          // HTTP error
          assertThrows(ExecutionException.class, () -> {
            final var service = new HttpServiceImplementation(new Parameters(
                "http",
                1.0f,
                false,
                "http",
                "localhost",
                8000,
                "/error",
                Method.POST));

            service.invoke(new ArrayList<ContextVariable>()).get();
          });

          // Invalid response type
          assertThrows(ExecutionException.class, () -> {
            final var service = new HttpServiceImplementation(new Parameters(
                "http",
                1.0f,
                false,
                "http",
                "localhost",
                8000,
                "/broken-response1",
                Method.POST));

            service.invoke(new ArrayList<ContextVariable>()).get();
          });

          // Invalid response type
          assertThrows(ExecutionException.class, () -> {
            final var service = new HttpServiceImplementation(new Parameters(
                "http",
                1.0f,
                false,
                "http",
                "localhost",
                8000,
                "/broken-response2",
                Method.POST));

            service.invoke(new ArrayList<ContextVariable>()).get();
          });
        });
  }
}
