package at.ac.uibk.dps.cirrina.runtime.service.description;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;

/**
 * Service implementation description, represents an abstract service implementation description. Needs to be specialized.
 *
 * @see HttpServiceImplementationDescription
 */
@JsonDeserialize(using = ServiceImplementationDescription.ServiceDescriptionDeserializer.class)
public abstract class ServiceImplementationDescription {

  /**
   * The name of the service implementation description.
   * <p>
   * The name is used to match a service type to a service implementation.
   */
  @NotNull
  public String name;

  /**
   * The type of service implementation.
   */
  @NotNull
  public ServiceImplementationType type;

  /**
   * Static cost of the service implementation.
   */
  @NotNull
  public float cost;

  /**
   * Whether this service implementation is a local service implementation.
   */
  @NotNull
  public boolean local;

  static class ServiceDescriptionDeserializer extends JsonDeserializer<ServiceImplementationDescription> {

    @Override
    public ServiceImplementationDescription deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      var codec = parser.getCodec();
      var treeNode = parser.readValueAsTree();

      var typeClasses = Map.of(
          "http", HttpServiceImplementationDescription.class
      );

      var type = codec.treeToValue(treeNode.get("type"), String.class);
      if (!typeClasses.containsKey(type)) {
        throw new IllegalArgumentException(String.format("Service description type '%s' is not known", type));
      }

      return parser.getCodec().treeToValue(treeNode, typeClasses.get(type));
    }
  }
}
