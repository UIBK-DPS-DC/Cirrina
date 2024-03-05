package ac.at.uibk.dps.nexa.lang.parser.classes.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class MatchActionClass extends ActionClass {

  @NotNull
  public String value;

  @JsonProperty("case")
  public Map<String, ActionOrActionReferenceClass> casee;
}
