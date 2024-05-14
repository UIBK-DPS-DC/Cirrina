package at.ac.uibk.dps.cirrina.execution.object.event;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class EventHandler implements AutoCloseable {

  private final List<EventListener> listeners = new ArrayList<>();

  private final ReentrantLock lock = new ReentrantLock();

  public abstract void sendEvent(Event event, String source) throws CirrinaException;

  public void addListener(EventListener listener) {
    try {
      lock.lock();

      listeners.add(listener);
    } finally {
      lock.unlock();
    }
  }

  public void removeListener(EventListener listener) {
    try {
      lock.lock();

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
    try {
      lock.lock();

      listeners.stream()
          .forEach(listener -> listener.onReceiveEvent(event));
    } finally {
      lock.unlock();
    }
  }
}