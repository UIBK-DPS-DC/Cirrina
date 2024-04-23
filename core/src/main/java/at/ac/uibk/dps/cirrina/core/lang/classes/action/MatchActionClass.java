package at.ac.uibk.dps.cirrina.core.lang.classes.action;

import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class MatchActionClass extends ActionClass {

  @NotNull
  public ExpressionClass value;

  @NotNull
  public List<MatchCase> cases;
}
