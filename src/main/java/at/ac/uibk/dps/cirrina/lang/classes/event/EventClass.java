package at.ac.uibk.dps.cirrina.lang.classes.event;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public final class EventClass {

  @NotNull
  public String name;

  @NotNull
  public EventChannel channel;

  public Map<String, String> data = Map.of();
}
