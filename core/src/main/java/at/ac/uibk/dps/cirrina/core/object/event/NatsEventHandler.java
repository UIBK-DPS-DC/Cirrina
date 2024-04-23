package at.ac.uibk.dps.cirrina.core.object.event;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Nats;
import java.io.IOException;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NatsEventHandler extends EventHandler {

  private static final Logger logger = LogManager.getLogger();
  private final Connection connection;
  private final Dispatcher dispatcher;

  public NatsEventHandler(String natsUrl) throws CirrinaException {
    // Attempt to connect to the NATS server
    try {
      connection = Nats.connect(natsUrl);
    } catch (InterruptedException | IOException e) {
      Thread.currentThread().interrupt();

      throw CirrinaException.from("Could not connect to the NATS server: %s", e.getMessage());
    }

    // Create a message dispatcher (asynchronous)
    dispatcher = connection.createDispatcher(this::handle);
  }

  private void handle(Message message) {
    // Reconstruct the event from the message data, if possible
    try {
      var event = Event.fromBytes(message.getData());

      propagateEvent(event);
    } catch (CirrinaException e) {
      logger.debug("A message could not be read as an event: {}", e.getMessage());
    }
  }

  @Override
  public void sendEvent(Event event, String source) throws CirrinaException {
    try {
      var data = event.toBytes();

      // * is used as a wildcard, for more information, refer to the NATS documentation:
      // https://docs.nats.io/using-nats/developer/receiving/wildcards
      var subject = Stream.of(event.getChannel())
          .map(channel -> {
            return switch (channel) {
              case EXTERNAL -> String.format("%s.%s", source, event.getName());
              case GLOBAL -> String.format("*.%s", event.getName());
              default -> throw new IllegalArgumentException(String.format("Unsupported channel '%s'", channel));
            };
          })
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("No channel specified for the event"));

      connection.publish(subject, data);
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw CirrinaException.from("Could not send event through NATS: %s", e.getMessage());
    }
  }

  @Override
  public void subscribe(String eventName) {
    dispatcher.subscribe(String.format("*.%s", eventName));
  }

  @Override
  public void unsubscribe(String eventName) {
    dispatcher.unsubscribe(String.format("*.%s", eventName));
  }

  @Override
  public void subscribe(String source, String eventName) {
    dispatcher.subscribe(String.format("%s.%s", source, eventName));
  }

  @Override
  public void unsubscribe(String source, String eventName) {
    dispatcher.unsubscribe(String.format("%s.%s", source, eventName));
  }

  @Override
  public void close() throws Exception {
    try {
      connection.close();
    } catch (InterruptedException e) {
      throw CirrinaException.from("Failed to close NATS persistent context: %s", e.getMessage());
    }
  }
}
