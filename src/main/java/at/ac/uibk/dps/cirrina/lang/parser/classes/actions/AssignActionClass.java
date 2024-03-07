package at.ac.uibk.dps.cirrina.lang.parser.classes.actions;

import at.ac.uibk.dps.cirrina.lang.parser.classes.context.ContextVariableClass;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class AssignActionClass extends ActionClass {

  @NotNull
  public ContextVariableClass variable;
}
