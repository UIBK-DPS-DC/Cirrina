package at.ac.uibk.dps.cirrina.nats;

import at.ac.uibk.dps.cirrina.core.CoreException;
import at.ac.uibk.dps.cirrina.core.objects.context.InMemoryContext;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.KeyValue;
import io.nats.client.Nats;
import io.nats.client.api.KeyValueEntry;
import io.nats.client.api.KeyValueWatcher;
import io.nats.client.impl.NatsKeyValueWatchSubscription;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An in-memory context, where context variables are contained in a hash map.
 */
public final class NatsPersistentContext extends InMemoryContext implements KeyValueWatcher,
    AutoCloseable {

  /**
   * The persistent context bucket name.
   */
  public final String PERSISTENT_CONTEXT_BUCKET_NAME = "persistentContext";

  private final Connection connection;

  private final KeyValue keyValue;

  private final Map<String, NatsKeyValueWatchSubscription> watchSubscriptions;

  /**
   * Initializes an empty persistent context.
   *
   * @param natsUrl NATS server URL.
   */
  public NatsPersistentContext(String natsUrl) throws CoreException {
    // Attempt to connect to the NATS server
    try {
      connection = Nats.connect(natsUrl);
    } catch (InterruptedException | IOException e) {
      throw new CoreException(
          String.format("Could not connect to the NATS server: %s", e.getCause()));
    }

    // Attempt to retrieve the bucket, which is expected to be pre-created. We do not manage the creation/deletion of
    // buckets
    try {
      keyValue = connection.keyValue(PERSISTENT_CONTEXT_BUCKET_NAME);
    } catch (IOException e) {
      throw new CoreException(
          "Failed to retrieve the persistent context bucket, make sure that it has been created");
    }

    watchSubscriptions = new HashMap<>();
  }


  /**
   * Retrieve a context variable.
   *
   * @param name Name of the context variable.
   * @return The retrieved context variable.
   * @throws CoreException If the context variable could not be retrieved.
   */
  @Override
  public ContextVariable get(String name) throws CoreException {
    return super.get(name);
  }

  /**
   * Creates a context variable.
   *
   * @param name  Name of the context variable.
   * @param value Value of the context variable.
   * @return The created context variable.
   * @throws CoreException If the variable could not be created.
   */
  @Override
  public ContextVariable create(String name, Object value) throws CoreException {
    try (var bs = new ByteArrayOutputStream(); var os = new ObjectOutputStream(bs)) {
      var variable = super.create(name, value);

      // Acquire serialize to bytes
      os.writeObject(variable.getValue());

      var bytes = bs.toByteArray();

      // Create the key
      keyValue.create(variable.name, bytes);

      // Watch the newly created key
      watchSubscriptions.put(variable.name, keyValue.watch(variable.name, this));

      return variable;
    } catch (IOException | JetStreamApiException | InterruptedException e) {
      throw new CoreException(
          String.format("Failed to create variable '%s': %s", name, e.getCause()));
    }
  }

  /**
   * Synchronize a variable in the context.
   *
   * @param variable Variable to synchronize.
   * @throws CoreException In case synchronization fails.
   */
  @Override
  protected void sync(ContextVariable variable) throws CoreException {
    super.sync(variable);
  }

  /**
   * Returns all context variables.
   *
   * @return Context variables.
   */
  @Override
  public List<ContextVariable> getAll() {
    return super.getAll();
  }

  /**
   * Called when an object has been updated.
   *
   * @param keyValueEntry The watched object.
   */
  @Override
  public void watch(KeyValueEntry keyValueEntry) {
    var variableName = keyValueEntry.getKey();

    if (variables.containsKey(variableName)) {
      // Acquire the variable
      var variable = variables.get(variableName);

      // Acquire the byte data
      var bytes = keyValueEntry.getValue();

      // Deserialize the byte data and attempt to update the variable
      try (ByteArrayInputStream bis = new ByteArrayInputStream(
          bytes); ObjectInputStream ois = new ObjectInputStream(bis)) {
        var object = ois.readObject();
        variable.setValue(object);
      } catch (IOException | CoreException | ClassNotFoundException e) {
        throw new RuntimeException(
            String.format("Failed to update variable '%s': %s", variable.name, e.getCause()));
      }
    }
  }

  /**
   * Called once if there is no data when the watch is created or if there is data, the first time
   * the watch exhausts all existing data.
   */
  @Override
  public void endOfData() {
  }

  /**
   * Clean up the persistent context. The creates keys will be deleted.
   *
   * @throws CoreException If an error occurred during cleaning up.
   */
  @Override
  public void close() throws Exception {
    // Remove the created variables
    for (var variable : getAll()) {
      try {
        // Unsubscribe and remove the watcher
        watchSubscriptions.remove(variable.name).close();

        // Remove the key
        keyValue.delete(variable.name);
      } catch (Exception e) {
        throw new CoreException(
            String.format("Failed to remove variable '%s': %s", variable.name, e.getCause()));
      }
    }

    // Close the connection
    try {
      connection.close();
    } catch (InterruptedException e) {
      throw new CoreException(
          String.format("Failed to close the connection to the NATS server: %s", e.getCause()));
    }
  }
}
