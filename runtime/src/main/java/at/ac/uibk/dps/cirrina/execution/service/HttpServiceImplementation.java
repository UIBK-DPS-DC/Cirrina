package at.ac.uibk.dps.cirrina.execution.service;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariableBuilder;
import at.ac.uibk.dps.cirrina.execution.service.description.HttpServiceImplementationDescription.Method;
import com.google.common.io.ByteStreams;
import io.fury.Fury;
import io.fury.config.Language;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HTTP service implementation, a service implementation that is accessible through HTTP.
 * <p>
 * Input variables provided to the invoked service are provided as a map of string-object. Input may be deserialized cross-language inside
 * an invoked service using Fury
 * <p>
 * Output variables received from the invoked service are provided as a map of string-object. Output is serialized cross-language using
 * Fury.
 *
 * @see <a href="https://fury.apache.org>Fury</a>
 */
public class HttpServiceImplementation extends ServiceImplementation {

  /**
   * HTTP scheme.
   */
  private final String scheme;

  /**
   * HTTP host.
   */
  private final String host;

  /**
   * HTTP port.
   */
  private final int port;

  /**
   * HTTP end-point,
   */
  private final String endPoint;

  /**
   * HTTP method.
   */
  private final Method method;

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
    this.method = parameters.method;
  }

  /**
   * Handle a service invocation response.
   *
   * @param connection HTTP connection.
   * @return Output variables.
   * @throws CirrinaException In case of error.
   */
  private List<ContextVariable> handleResponse(HttpURLConnection connection) throws CirrinaException {
    try {
      final var fury = Fury.builder()
          .withLanguage(Language.XLANG)
          .requireClassRegistration(false)
          .build();

      // Get response
      final var errorCode = connection.getResponseCode();

      switch (errorCode) {
        case HttpURLConnection.HTTP_OK -> {
          try (final var inputStream = connection.getInputStream()) {
            // Acquire the payload
            final var payload = ByteStreams.toByteArray(inputStream);

            // Perform deserialization
            final var output = fury.deserialize(payload);

            // Verify the output, we expect a map of string-object
            if (!(output instanceof Map<?, ?> untypedMap)) {
              throw CirrinaException.from("Unexpected HTTP service invocation type, expected map of string-object");
            }
            if (!untypedMap.isEmpty()) {
              if (!untypedMap.entrySet().stream()
                  .allMatch(entry -> entry.getKey() instanceof String
                      && entry.getValue() != null)) {
                throw CirrinaException.from("Unexpected HTTP service invocation type, expected map of string-object");
              }
            }

            @SuppressWarnings("unchecked") final var map = (Map<String, Object>) output;

            // Build the output variables
            final var builder = ContextVariableBuilder.from();

            return map.entrySet().stream()
                .map(entry -> builder.name(entry.getKey()).value(entry.getValue()))
                .map(ContextVariableBuilder::build)
                .toList();
          } catch (IOException e) {
            throw CirrinaException.from("Could not read the response body: %s", e.getMessage());
          }
        }
        default -> throw CirrinaException.from("Received HTTP error (%d)", errorCode);
      }
    } catch (IOException e) {
      throw CirrinaException.from("Failed to handle response: %s", e.getMessage());
    }
  }

  /**
   * Invoke this service implementation.
   *
   * @param input Input to the service invocation.
   * @return The service invocation output.
   * @throws CirrinaException If the service invocation failed.
   */
  @Override
  public List<ContextVariable> invoke(List<ContextVariable> input) throws CirrinaException {
    HttpURLConnection connection = null;

    try {
      final var fury = Fury.builder()
          .withLanguage(Language.XLANG)
          .requireClassRegistration(false)
          .build();

      if (input.stream().anyMatch(ContextVariable::isLazy)) {
        throw CirrinaException.from("All variables need to be evaluated before service input can be converted to bytes");
      }

      final var payload = fury.serialize(input.stream()
          .collect(Collectors.toMap(ContextVariable::name, ContextVariable::value)));

      // Create URL
      final var url = new URI(scheme, "", host, port, endPoint, "", "").toURL();

      connection = (HttpURLConnection) url.openConnection();

      // Set HTTP method
      switch (method) {
        case GET -> connection.setRequestMethod("GET");
        case POST -> connection.setRequestMethod("POST");
      }

      // Set request properties
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type", "application/octet-stream");
      connection.setRequestProperty("Content-Length", String.valueOf(payload.length));

      // Write request body
      try (OutputStream outputStream = connection.getOutputStream()) {
        outputStream.write(payload);
      }

      return handleResponse(connection);
    } catch (IOException | URISyntaxException e) {
      throw CirrinaException.from("Failed to perform HTTP service invocation: %s", e.getMessage());
    } finally {
      // Close connection
      if (connection != null) {
        connection.disconnect();
      }
    }
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

  public record Parameters(
      String name,
      float cost,
      boolean local,
      String scheme,
      String host,
      int port,
      String endPoint,
      Method method
  ) {

  }
}
