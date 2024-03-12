package at.ac.uibk.dps.cirrina.lang.parser.classes.actions;

import at.ac.uibk.dps.cirrina.lang.parser.classes.ExpressionClass;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class TimeoutActionClass extends ActionClass {

  @NotNull
  public ExpressionClass delay;

  @NotNull
  public List<ActionOrActionReferenceClass> actions;
}
