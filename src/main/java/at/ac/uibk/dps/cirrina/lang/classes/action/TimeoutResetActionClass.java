package at.ac.uibk.dps.cirrina.lang.classes.action;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class TimeoutResetActionClass extends ActionClass {

  @NotNull
  public String action;
}
