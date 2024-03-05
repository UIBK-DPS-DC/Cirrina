package ac.at.uibk.dps.nexa.lang.parser.classes;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;

public class EventClass {

  @NotNull
  public String name;

  @NotNull
  public EventChannel channel;

  public Optional<Map<String, String>> data = Optional.empty();
}
