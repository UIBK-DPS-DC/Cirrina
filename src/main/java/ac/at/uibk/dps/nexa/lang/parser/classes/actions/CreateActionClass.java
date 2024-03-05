package ac.at.uibk.dps.nexa.lang.parser.classes.actions;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class CreateActionClass extends ActionClass {

  @NotNull
  public String variable;

  @NotNull
  public String value;

  @JsonSetter(nulls = Nulls.SKIP)
  public boolean isPersistent = false;
}
