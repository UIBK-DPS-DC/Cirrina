package at.ac.uibk.dps.cirrina.execution.service;

import at.ac.uibk.dps.cirrina.execution.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.execution.object.context.ContextVariableBuilder;
import at.ac.uibk.dps.cirrina.execution.service.description.HttpServiceImplementationDescription.Method;
import io.fury.Fury;
import io.fury.config.Language;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
   * @param response HTTP response.
   * @return Output variables.
   * @throws CompletionException In case of error.
   */
  private static List<ContextVariable> handleResponse(HttpResponse<byte[]> response) {
    final var fury = Fury.builder()
        .withLanguage(Language.XLANG)
        .requireClassRegistration(false)
        .build();

    // Require HTTP OK
    final var errorCode = response.statusCode();

    if (errorCode != HttpURLConnection.HTTP_OK) {
      throw new CompletionException(new IOException("HTTP error (%d)".formatted(errorCode)));
    }

    // Acquire the payload
    final var payload = response.body();

    // Perform deserialization
    final var output = fury.deserialize(payload);

    // Verify the output, we expect a map of string-object
    if (!(output instanceof Map<?, ?> untypedMap)) {
      throw new CompletionException(
          new IOException("Unexpected HTTP service invocation type, expected map of string-object"));
    }
    if (!untypedMap.isEmpty()) {
      if (!untypedMap.entrySet().stream()
          .allMatch(entry -> entry.getKey() instanceof String
              && entry.getValue() != null)) {
        throw new CompletionException(
            new IOException("Unexpected HTTP service invocation type, expected map of string-object"));
      }
    }

    @SuppressWarnings("unchecked") final var map = (Map<String, Object>) output;

    // Build the output variables
    final var builder = ContextVariableBuilder.from();

    return map.entrySet().stream()
        .map(entry -> builder.name(entry.getKey()).value(entry.getValue()))
        .map(ContextVariableBuilder::build)
        .toList();
  }

  /**
   * Invoke this service implementation.
   * <p>
   * All input variables must be evaluated.
   *
   * @param input Input to the service invocation.
   * @return The service invocation output.
   * @throws UnsupportedOperationException If not all variables are evaluated.
   * @throws UnsupportedOperationException If the invocation failed.
   */
  @Override
  public CompletableFuture<List<ContextVariable>> invoke(List<ContextVariable> input) throws UnsupportedOperationException {
    try (final var client = HttpClient.newHttpClient()) {
      final var fury = Fury.builder()
          .withLanguage(Language.XLANG)
          .requireClassRegistration(false)
          .build();

      if (input.stream().anyMatch(ContextVariable::isLazy)) {
        throw new UnsupportedOperationException("All variables need to be evaluated before service input can be converted to bytes");
      }

      final var payload = fury.serialize(input.stream()
          .collect(Collectors.toMap(ContextVariable::name, ContextVariable::value)));

      // Create URL
      final var uri = new URI(scheme, "", host, port, endPoint, "", "");

      final var request = HttpRequest.newBuilder()
          .version(HttpClient.Version.HTTP_2)
          .method(method.toString(), BodyPublishers.ofByteArray(payload))
          .uri(uri)
          .build();

      return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).thenApplyAsync(HttpServiceImplementation::handleResponse);
    } catch (URISyntaxException e) {
      throw new UnsupportedOperationException("Failed to perform HTTP service invocation", e);
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
