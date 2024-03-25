package at.ac.uibk.dps.cirrina.object.event;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class EventHandler implements AutoCloseable {

  private final List<EventListener> listeners = new ArrayList<>();
  private final ReentrantLock lock = new ReentrantLock();

  public abstract void sendEvent(URI source, Event event);

  public void addListener(EventListener listener) {
    lock.lock();

    try {
      listeners.add(listener);
    } finally {
      lock.unlock();
    }
  }

  public void removeListener(EventListener listener) {
    lock.lock();

    try {
      listeners.remove(listener);
    } finally {
      lock.unlock();
    }
  }

  public abstract void subscribe(String topic);

  public abstract void unsubscribe(String topic);

  protected void propagateEvent(Event event) {
    lock.lock();

    try {
      listeners.stream()
          .forEach(listener -> listener.onReceiveEvent(event));
    } finally {
      lock.unlock();
    }
  }
}
