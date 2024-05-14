package at.ac.uibk.dps.cirrina.csml.description.action;

import at.ac.uibk.dps.cirrina.csml.description.ExpressionDescription;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class MatchActionDescription extends ActionDescription {

  @NotNull
  public ExpressionDescription value;

  @NotNull
  public List<MatchCaseDescription> cases;
}
