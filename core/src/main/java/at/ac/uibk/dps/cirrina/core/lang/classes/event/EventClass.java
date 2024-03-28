package at.ac.uibk.dps.cirrina.core.lang.classes.event;

import at.ac.uibk.dps.cirrina.core.lang.classes.context.ContextVariableClass;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class EventClass {

  @NotNull
  public String name;

  @NotNull
  public EventChannel channel;

  public List<ContextVariableClass> data = List.of();
}
