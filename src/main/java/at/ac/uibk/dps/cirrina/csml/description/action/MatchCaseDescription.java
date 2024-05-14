package at.ac.uibk.dps.cirrina.csml.description.action;

import at.ac.uibk.dps.cirrina.csml.description.ExpressionDescription;
import at.ac.uibk.dps.cirrina.csml.description.helper.ActionOrActionReferenceDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class MatchCaseDescription {

  @NotNull
  @JsonProperty("case")
  public ExpressionDescription casee;

  @NotNull
  public ActionOrActionReferenceDescription action;
}
