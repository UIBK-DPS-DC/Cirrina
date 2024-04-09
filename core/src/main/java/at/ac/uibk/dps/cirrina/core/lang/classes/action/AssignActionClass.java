package at.ac.uibk.dps.cirrina.core.lang.classes.action;

import at.ac.uibk.dps.cirrina.core.lang.classes.context.ContextVariableClass;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class AssignActionClass extends ActionClass {

  @NotNull
  public ContextVariableClass variable;
}