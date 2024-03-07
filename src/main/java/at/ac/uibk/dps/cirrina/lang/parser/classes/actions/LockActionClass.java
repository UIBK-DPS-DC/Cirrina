package at.ac.uibk.dps.cirrina.lang.parser.classes.actions;

import at.ac.uibk.dps.cirrina.lang.parser.classes.context.ContextVariableReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class LockActionClass extends ActionClass {

  @NotNull
  public ContextVariableReference variable;
}
