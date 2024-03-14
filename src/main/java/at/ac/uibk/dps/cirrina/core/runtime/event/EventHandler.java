package at.ac.uibk.dps.cirrina.core.runtime.event;

import java.util.ArrayList;
import java.util.List;

public abstract class EventHandler {

  private final List<EventListener> listeners = new ArrayList<>();

  public abstract void sendEvent(Event event);

  public void addListener(EventListener listener) {
    listeners.add(listener);
  }

  public void removeListener(EventListener listener) {
    listeners.remove(listener);
  }

  protected void propogateEvent(Event event) {
    listeners.stream()
        .forEach(listener -> listener.onReceiveEvent(event));
  }
}
