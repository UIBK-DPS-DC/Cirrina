package at.ac.uibk.dps.cirrina.execution.service;

import at.ac.uibk.dps.cirrina.execution.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.execution.object.exchange.ContextVariableExchange;
import at.ac.uibk.dps.cirrina.execution.object.exchange.ContextVariableProtos;
import at.ac.uibk.dps.cirrina.execution.service.description.HttpServiceImplementationDescription.Method;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;

/**
 * HTTP service implementation, a service implementation that is accessible through HTTP.
 * <p>
 * Input variables provided to the invoked service are provided as a list of context variables.
 * <p>
 * Context variables are encoded using Protocol Buffers.
 */
public class HttpServiceImplementation extends ServiceImplementation {

  /**
   * HTTP client.
   */
  private static final CloseableHttpAsyncClient httpClient;

  static {
    final var connectionManager = new PoolingAsyncClientConnectionManager();
    connectionManager.setMaxTotal(100);
    connectionManager.setDefaultMaxPerRoute(100);

    httpClient = HttpAsyncClients.custom()
        .setConnectionManager(connectionManager)
        .build();
    httpClient.start();
  }

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
  private static List<ContextVariable> handleResponse(SimpleHttpResponse response) {
    // Require HTTP OK
    final var errorCode = response.getCode();

    if (errorCode != 200) {
      throw new CompletionException(new IOException("HTTP error (%d)".formatted(errorCode)));
    }

    // Acquire the payload
    final var payload = response.getBodyBytes();

    try {
      // Empty payload
      if (payload.length == 0) {
        return List.of();
      }

      // Otherwise we expect a serialized collection of context variables
      return ContextVariableProtos.ContextVariables.parseFrom(payload)
          .getDataList().stream()
          .map(ContextVariableExchange::fromProto)
          .toList();
    } catch (InvalidProtocolBufferException e) {
      throw new CompletionException(
          new IOException("Unexpected HTTP service invocation value type"));
    }
  }

  /**
   * Invoke this service implementation.
   * <p>
   * All input variables must be evaluated.
   *
   * @param input Input to the service invocation.
   * @param id    Sender ID.
   * @return The service invocation output.
   * @throws UnsupportedOperationException If not all variables are evaluated.
   * @throws UnsupportedOperationException If the invocation failed.
   */
  @Override
  public CompletableFuture<List<ContextVariable>> invoke(List<ContextVariable> input, String id) throws UnsupportedOperationException {
    try {
      if (input.stream().anyMatch(ContextVariable::isLazy)) {
        throw new UnsupportedOperationException("All variables need to be evaluated before service input can be converted to bytes");
      }

      // Serialize the data
      final byte[] payload = input.isEmpty()
          ? new byte[0] // Send no data if there are no input variables, avoid serialization of nothing
          : ContextVariableProtos.ContextVariables.newBuilder()
              .addAllData(input.stream()
                  .map(contextVariable -> new ContextVariableExchange(contextVariable).toProto())
                  .toList()
              )
              .build()
              .toByteArray();

      // Create URL
      final var uri = new URI(scheme, null, host, port, endPoint, null, null);

      final var request = new SimpleHttpRequest(method.toString(), uri);
      request.setBody(payload, ContentType.APPLICATION_OCTET_STREAM);
      request.setHeader("Cirrina-Sender-ID", id);

      final CompletableFuture<List<ContextVariable>> future = new CompletableFuture<>();

      httpClient.execute(request, new FutureCallback<SimpleHttpResponse>() {
        @Override
        public void completed(SimpleHttpResponse response) {
          try {
            future.complete(handleResponse(response));
          } catch (Exception e) {
            future.completeExceptionally(e);
          }
        }

        @Override
        public void failed(Exception ex) {
          future.completeExceptionally(ex);
        }

        @Override
        public void cancelled() {
          future.completeExceptionally(new IOException("Request cancelled"));
        }
      });

      return future;
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

  /**
   * Returns a string for informative purposes (a service implementation is abstract, so can provide no information about the details).
   *
   * @return Information string.
   */
  @Override
  public String getInformationString() {
    try {
      final var uri = new URI(scheme, null, host, port, endPoint, null, null);

      return "%s".formatted(uri.toString());
    } catch (URISyntaxException e) {
      return "Invalid information string: %s".formatted(e.getMessage());
    }
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
