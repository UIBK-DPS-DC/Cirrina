package at.ac.uibk.dps.cirrina.core.objects;

import at.ac.uibk.dps.cirrina.lang.parser.classes.events.EventChannel;
import java.util.Map;

public final class Event {


  public final String name;
  public final EventChannel channel;
  public final Map<String, String> data;

  public Event(String name, EventChannel channel, Map<String, String> data) {
    this.name = name;
    this.channel = channel;
    this.data = data;
  }

  @Override
  public String toString() {
    return name;
  }
}
