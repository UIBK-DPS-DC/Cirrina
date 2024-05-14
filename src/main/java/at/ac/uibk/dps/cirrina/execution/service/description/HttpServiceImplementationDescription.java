package at.ac.uibk.dps.cirrina.execution.service.description;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * HTTP service implementation description, represents an available service implementation that is accessible via HTTP.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class HttpServiceImplementationDescription extends ServiceImplementationDescription {

  /**
   * The HTTP scheme.
   */
  @NotNull
  public String scheme;

  /**
   * The HTTP host.
   */
  @NotNull
  public String host;

  /**
   * The HTTP port.
   */
  @Min(value = 0, message = "Port must be in range [1, 65535]")
  @Max(value = 65535, message = "Port must be in range [1, 65535]")
  public int port;

  /**
   * The HTTP endpoint.
   */
  @NotNull
  public String endPoint;

  /**
   * The HTTP method.
   */
  @NotNull
  public Method method;

  public enum Method {
    @JsonProperty("GET")
    GET,
    @JsonProperty("POST")
    POST;

    @Override
    public String toString() {
      return switch (this) {
        case GET -> "GET";
        case POST -> "POST";
      };
    }
  }
}
