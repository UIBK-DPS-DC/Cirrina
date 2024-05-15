package at.ac.uibk.dps.cirrina.execution.object.event;

import at.ac.uibk.dps.cirrina.csml.keyword.EventChannel;
import at.ac.uibk.dps.cirrina.execution.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.execution.object.context.Extent;
import io.fury.Fury;
import io.fury.config.Language;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Event, resembles an event as it is sent to state machine instances.
 */
public final class Event {

  /**
   * Event ID, is unique.
   */
  private final String id = UUID.randomUUID().toString();

  /**
   * Name of the event, is not unique.
   */
  private final String name;

  /**
   * Event channel.
   */
  private final EventChannel channel;

  /**
   * Event data.
   */
  private final List<ContextVariable> data;

  /**
   * Initializes this event. A random ID will be assigned to identify this event.
   *
   * @param name    Event name.
   * @param channel Event channel.
   * @param data    Event data.
   */
  Event(String name, EventChannel channel, List<ContextVariable> data) {
    this.name = name;
    this.channel = channel;
    this.data = data;
  }

  /**
   * (Re)constructs an event from byte data.
   *
   * @param bytes Byte data.
   * @return Event.
   * @throws UnsupportedOperationException In case the event is not in a recognizable format.
   */
  public static Event fromBytes(byte[] bytes) throws UnsupportedOperationException {
    final var fury = Fury.builder()
        .withLanguage(Language.XLANG)
        .requireClassRegistration(false)
        .build();

    final var event = fury.deserialize(bytes);

    if (!(event instanceof Event)) {
      throw new UnsupportedOperationException("Received an event with an unsupported payload");
    }

    return (Event) event;
  }

  /**
   * Ensure that an event only has evaluated event data.
   *
   * @param event  Event.
   * @param extent Extent.
   * @return Event with evaluated event data.
   * @throws UnsupportedOperationException If a event data variable could not be evaluated.
   */
  public static Event ensureHasEvaluatedData(Event event, Extent extent) throws UnsupportedOperationException {
    var data = new ArrayList<ContextVariable>();

    for (var variable : event.getData()) {
      try {
        data.add(variable.evaluate(extent));
      } catch (UnsupportedOperationException e) {
        throw new UnsupportedOperationException("The event data variable '%s' could not be evaluated".formatted(
            variable.name()), e);
      }
    }

    return new Event(event.getName(), event.getChannel(), data);
  }

  public Event withData(List<ContextVariable> data) {
    return new Event(name, channel, data);
  }

  /**
   * Returns the ID.
   *
   * @return ID.
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the name.
   *
   * @return Name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the channel.
   *
   * @return Channel.
   */
  public EventChannel getChannel() {
    return channel;
  }

  /**
   * Returns the data.
   *
   * @return Data.
   */
  public List<ContextVariable> getData() {
    return data;
  }

  /**
   * Returns a string representation.
   *
   * @return String representation.
   */
  @Override
  public String toString() {
    return name;
  }

  /**
   * Returns a hash code representation.
   *
   * @return Hash code representation.
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, channel, data);
  }

  /**
   * Converts this event to byte data.
   * <p>
   * All data variables of this event must be evaluated.
   *
   * @return Byte data.
   * @throws IllegalStateException If the event has unevaluated event data.
   */
  public byte[] toBytes() throws IllegalStateException {
    if (data.stream().anyMatch(ContextVariable::isLazy)) {
      throw new IllegalStateException("Event '%s' has unevaluated event data".formatted(name));
    }

    Fury fury = Fury.builder()
        .withLanguage(Language.XLANG)
        .requireClassRegistration(false)
        .build();

    return fury.serialize(this);
  }
}
