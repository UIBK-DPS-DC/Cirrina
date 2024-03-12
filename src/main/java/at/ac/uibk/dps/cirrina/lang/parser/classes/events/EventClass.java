package at.ac.uibk.dps.cirrina.lang.parser.classes.events;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;

public final class EventClass {

  @NotNull
  public String name;

  @NotNull
  public EventChannel channel;

  public Optional<Map<String, String>> data = Optional.empty();
}
