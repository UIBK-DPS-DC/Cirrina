package at.ac.uibk.dps.cirrina.object.event;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class EventHandler implements AutoCloseable {

  private final List<EventListener> listeners = new ArrayList<>();
  private final ReentrantLock lock = new ReentrantLock();

  public abstract void sendEvent(Event event, String source) throws RuntimeException;

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

  public abstract void subscribe(String subject);

  public abstract void unsubscribe(String subject);

  public abstract void subscribe(String source, String subject);

  public abstract void unsubscribe(String source, String subject);

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
