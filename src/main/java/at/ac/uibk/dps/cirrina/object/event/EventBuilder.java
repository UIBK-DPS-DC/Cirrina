package at.ac.uibk.dps.cirrina.object.event;

import at.ac.uibk.dps.cirrina.lang.classes.context.ContextVariableClass;
import at.ac.uibk.dps.cirrina.lang.classes.event.EventClass;
import at.ac.uibk.dps.cirrina.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.object.context.ContextVariableBuilder;
import java.util.List;

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

  private static List<ContextVariable> buildVariableList(List<ContextVariableClass> contextVariableClasses) {
    return contextVariableClasses.stream()
        .map(c -> ContextVariableBuilder.from(c).build())
        .toList();
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
        buildVariableList(eventClass.data)
    );
  }
}
