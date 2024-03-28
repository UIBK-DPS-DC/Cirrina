package at.ac.uibk.dps.cirrina.core.lang.classes.action;

import at.ac.uibk.dps.cirrina.core.lang.classes.Construct;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;


@JsonDeserialize(using = ActionClass.ActionDeserializer.class)
public class ActionClass extends Construct implements ActionOrActionReferenceClass {

  @NotNull
  public Type type;

  public Optional<String> name;

  public enum Type {
    @JsonProperty("invoke")
    INVOKE,

    @JsonProperty("create")
    CREATE,

    @JsonProperty("assign")
    ASSIGN,

    @JsonProperty("lock")
    LOCK,

    @JsonProperty("unlock")
    UNLOCK,

    @JsonProperty("raise")
    RAISE,

    @JsonProperty("timeout")
    TIMEOUT,

    @JsonProperty("timeoutReset")
    TIMEOUT_RESET,

    @JsonProperty("match")
    MATCH
  }

  static class ActionDeserializer extends JsonDeserializer<ActionClass> {

    @Override
    public ActionClass deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      var codec = parser.getCodec();
      var treeNode = parser.readValueAsTree();

      // Try to determine the action type that belongs to the type property.
      var typeClasses = Map.of(
          "assign", AssignActionClass.class,
          "create", CreateActionClass.class,
          "invoke", InvokeActionClass.class,
          "match", MatchActionClass.class,
          "raise", RaiseActionClass.class,
          "timeout", TimeoutActionClass.class,
          "timeoutReset", TimeoutResetActionClass.class
      );

      var type = codec.treeToValue(treeNode.get("type"), String.class);
      if (!typeClasses.containsKey(type)) {
        throw new IllegalArgumentException(String.format("Action type '%s' is not known", type));
      }

      // Return the appropriate action type. Action types are children of Action and would normally
      // invoke this deserializer. Therefore, the deserializer on action types should be set to none
      // for this to not result in an infinite recursion
      return parser.getCodec().treeToValue(treeNode, typeClasses.get(type));
    }
  }
}

