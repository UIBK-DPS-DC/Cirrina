package at.ac.uibk.dps.cirrina.core.object.builder;

import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.lang.classes.event.EventClass;

/**
 * Event builder, used to build event objects.
 */
public class EventBuilder {

  /**
   * The event class to build from.
   */
  private final EventClass eventClass;

  /**
   * Initializes an event builder.
   *
   * @param eventClass Event class.
   */
  private EventBuilder(EventClass eventClass) {
    this.eventClass = eventClass;
  }

  /**
   * Initializes an event builder.
   *
   * @param eventClass Event class.
   * @return Event builder.
   */
  public static EventBuilder from(EventClass eventClass) {
    return new EventBuilder(eventClass);
  }

  /**
   * Builds the event.
   *
   * @return The built event.
   */
  public Event build() {
    return new Event(
        eventClass.name,
        eventClass.channel,
        eventClass.data
    );
  }
}
