package at.ac.uibk.dps.cirrina.csml.description.event;

import at.ac.uibk.dps.cirrina.csml.description.context.ContextVariableDescription;
import at.ac.uibk.dps.cirrina.csml.keyword.EventChannel;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class EventDescription {

  @NotNull
  public String name;

  @NotNull
  public EventChannel channel;

  public List<ContextVariableDescription> data = List.of();
}
