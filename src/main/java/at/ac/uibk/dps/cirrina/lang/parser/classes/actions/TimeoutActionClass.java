package at.ac.uibk.dps.cirrina.lang.parser.classes.actions;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class TimeoutActionClass extends ActionClass {

  @NotNull
  public String delay;

  @NotNull
  public List<ActionOrActionReferenceClass> actions;
}
