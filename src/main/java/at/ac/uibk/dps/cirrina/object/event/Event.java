package at.ac.uibk.dps.cirrina.object.event;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.lang.classes.event.EventChannel;
import at.ac.uibk.dps.cirrina.object.context.ContextVariable;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.fury.Fury;
import io.fury.config.Language;
import java.net.URI;
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
   * (Re)constructs an event from a CloudEvents event.
   *
   * @param cloudEvent CloudEvents event.
   * @return Event.
   * @throws RuntimeException In case the event could not be retrieved from the CloudEvents event.
   */
  public static Event fromCloudEvent(CloudEvent cloudEvent) throws RuntimeException {
    Fury fury = Fury.builder()
        .withLanguage(Language.XLANG)
        .requireClassRegistration(false)
        .build();

    var event = fury.deserialize(cloudEvent.getData().toBytes());
    if (!(event instanceof Event)) {
      throw RuntimeException.from("Received a cloud event that does not contain an event as its data");
    }

    return (Event) event;
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
   * Converts this event to a CloudEvents event.
   *
   * @param source Source to include.
   * @return CloudEvents event.
   */
  public CloudEvent toCloudEvent(URI source) {
    Fury fury = Fury.builder()
        .withLanguage(Language.XLANG)
        .requireClassRegistration(false)
        .build();

    var data = fury.serialize(this);

    return CloudEventBuilder.v1()
        .withId(id)
        .withSource(source)
        .withData(data)
        .withType("at.ac.uibk.dps.cirrina.event")
        .build();
  }
}
