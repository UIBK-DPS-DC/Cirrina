package at.ac.uibk.dps.cirrina.object.event;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.lang.classes.event.EventChannel;
import at.ac.uibk.dps.cirrina.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.object.context.Extent;
import io.fury.Fury;
import io.fury.config.Language;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Event, resembles an event as it is sent to state machine instances. An event can be translated into a CloudEvents event for
 * transmission.
 */
public final class Event {

  private final String id = UUID.randomUUID().toString();
  private final String name;
  private final EventChannel channel;
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
   * @throws RuntimeException In case the event could not be retrieved from the CloudEvents event.
   */
  public static Event fromBytes(byte[] bytes) throws RuntimeException {
    Fury fury = Fury.builder()
        .withLanguage(Language.XLANG)
        .requireClassRegistration(false)
        .build();

    var event = fury.deserialize(bytes);
    if (!(event instanceof Event)) {
      throw RuntimeException.from("Received a cloud event that does not contain an event as its data");
    }

    return (Event) event;
  }

  public static Event ensureHasEvaluatedData(Event event, Extent extent) throws RuntimeException {
    var data = new ArrayList<ContextVariable>();

    for (var variable : event.getData()) {
      data.add(variable.evaluate(extent));
    }

    return new Event(event.getName(), event.getChannel(), data);
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
   *
   * @return Byte data.
   */
  public byte[] toBytes() throws RuntimeException {
    if (data.stream().anyMatch(ContextVariable::isLazy)) {
      throw RuntimeException.from("All variables need to be evaluated before an event can be converted to bytes");
    }

    Fury fury = Fury.builder()
        .withLanguage(Language.XLANG)
        .requireClassRegistration(false)
        .build();

    return fury.serialize(this);
  }
}
