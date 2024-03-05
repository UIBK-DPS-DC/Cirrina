package ac.at.uibk.dps.nexa.lang.parser.classes.actions;

import ac.at.uibk.dps.nexa.lang.parser.classes.EventClass;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class RaiseActionClass extends ActionClass {

  @NotNull
  public EventClass event;
}
