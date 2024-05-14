package at.ac.uibk.dps.cirrina.csml.description.action;

import at.ac.uibk.dps.cirrina.csml.description.ExpressionDescription;
import at.ac.uibk.dps.cirrina.csml.description.helper.ActionOrActionReferenceDescription;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class TimeoutActionDescription extends ActionDescription {

  @NotNull
  public ExpressionDescription delay;

  @NotNull
  public ActionOrActionReferenceDescription action;
}
