package at.ac.uibk.dps.cirrina.lang.parser.classes.actions;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class TimeoutResetActionClass extends ActionClass {

  @NotNull
  public String action;
}