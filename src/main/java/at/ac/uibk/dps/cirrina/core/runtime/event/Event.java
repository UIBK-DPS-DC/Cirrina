package at.ac.uibk.dps.cirrina.core.runtime.event;

import at.ac.uibk.dps.cirrina.core.runtime.context.Context.ContextVariable;
import at.ac.uibk.dps.cirrina.lang.classes.event.EventChannel;
import java.util.List;

public final class Event {


  public final String name;
  public final EventChannel channel;
  public final List<ContextVariable> data;

  Event(String name, EventChannel channel, List<ContextVariable> data) {
    this.name = name;
    this.channel = channel;
    this.data = data;
  }

  @Override
  public String toString() {
    return name;
  }
}
