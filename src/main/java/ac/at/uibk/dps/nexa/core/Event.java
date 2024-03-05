package ac.at.uibk.dps.nexa.core;

import ac.at.uibk.dps.nexa.lang.parser.classes.EventChannel;
import java.util.Map;
import java.util.Optional;

public class Event {


  public final String name;

  public final EventChannel channel;

  public final Optional<Map<String, String>> data;

  public Event(String name, EventChannel channel, Optional<Map<String, String>> data) {
    this.name = name;
    this.channel = channel;
    this.data = data;
  }
}
