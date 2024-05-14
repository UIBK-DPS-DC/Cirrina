package at.ac.uibk.dps.cirrina.csml.description.action;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class TimeoutResetActionDescription extends ActionDescription {

  @NotNull
  public String action;
}
