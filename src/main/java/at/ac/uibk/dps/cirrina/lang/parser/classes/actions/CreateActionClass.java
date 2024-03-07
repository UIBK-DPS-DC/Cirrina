package at.ac.uibk.dps.cirrina.lang.parser.classes.actions;

import at.ac.uibk.dps.cirrina.lang.parser.classes.context.ContextVariableClass;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CreateActionClass extends ActionClass {

  @NotNull
  public ContextVariableClass variable;

  @JsonSetter(nulls = Nulls.SKIP)
  public boolean isPersistent = false;
}
