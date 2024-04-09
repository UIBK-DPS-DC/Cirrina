package at.ac.uibk.dps.cirrina.core.lang.classes.action;

import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.helper.ActionOrActionReferenceClass;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class MatchActionClass extends ActionClass {

  @NotNull
  public ExpressionClass value;

  @JsonProperty("case")
  public Map<ExpressionClass, ActionOrActionReferenceClass> casee;
}
