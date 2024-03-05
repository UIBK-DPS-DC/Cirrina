package ac.at.uibk.dps.nexa.lang.parser.classes.actions;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class AssignActionClass extends ActionClass {

  @NotNull
  public String variable;

  @NotNull
  public String value;
}
