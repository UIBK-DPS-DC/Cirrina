package at.ac.uibk.dps.cirrina.nats;

import at.ac.uibk.dps.cirrina.core.CoreException;
import at.ac.uibk.dps.cirrina.core.objects.context.Context;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.KeyValue;
import io.nats.client.Nats;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * A persistent context containing within a NATS bucket.
 */
public final class NatsPersistentContext extends Context {

  /**
   * The persistent context bucket name.
   */
  public final String PERSISTENT_CONTEXT_BUCKET_NAME = "persistent";
  private final Connection connection;
  private final KeyValue keyValue;
  private final Vector<String> knownKeys = new Vector<>();

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
      Thread.currentThread().interrupt();

      throw new CoreException(
          String.format("Could not connect to the NATS server: %s", e.getMessage()));
    }

    // Attempt to retrieve the bucket, which is expected to be pre-created. We do not manage the creation/deletion of
    // buckets
    try {
      keyValue = connection.keyValue(PERSISTENT_CONTEXT_BUCKET_NAME);
    } catch (IOException e) {
      throw new CoreException(
          "Failed to retrieve the persistent context bucket, make sure that it has been created");
    }
  }


  /**
   * Retrieve a context variable.
   *
   * @param name Name of the context variable.
   * @return The retrieved context variable.
   * @throws CoreException If the context variable could not be retrieved.
   */
  @Override
  public Object get(String name) throws CoreException {
    try {
      if (!knownKeys.contains(name)) {
        throw new CoreException(
            String.format("A variable with the name '%s' does not exist", name));
      }

      var entry = keyValue.get(name);

      return fromBytes(entry.getValue());
    } catch (IOException | JetStreamApiException e) {
      throw new CoreException(
          String.format("Failed to retrieve the variable '%s': %s", name, e.getMessage()));
    }
  }

  /**
   * Creates a context variable.
   *
   * @param name  Name of the context variable.
   * @param value Value of the context variable.
   * @throws CoreException If the variable could not be created.
   */
  @Override
  public void create(String name, Object value) throws CoreException {
    try {
      if (knownKeys.contains(name)) {
        throw new CoreException(
            String.format("A variable with the name '%s' already exists", name));
      }

      keyValue.create(name, toBytes(value));
    } catch (IOException | JetStreamApiException e) {
      throw new CoreException(
          String.format("Failed to create variable '%s': %s", name, e.getMessage()));
    }
  }

  /**
   * Assigns to a context variable.
   *
   * @param name  Name of the context variable.
   * @param value New value of the context variable.
   * @throws CoreException If the variable could not be assigned to.
   */
  @Override
  public void assign(String name, Object value) throws CoreException {
    try {
      if (!knownKeys.contains(name)) {
        throw new CoreException(
            String.format("A variable with the name '%s' does not exist", name));
      }

      keyValue.put(name, toBytes(value));
    } catch (IOException | JetStreamApiException e) {
      Thread.currentThread().interrupt();

      throw new CoreException(
          String.format("Failed to assign to the variable '%s': %s", name, e.getMessage()));
    }
  }

  /**
   * Deletes a context variable.
   *
   * @param name Name of the context variable.
   * @throws CoreException If the variable could not be deleted.
   */
  @Override
  public void delete(String name, Object value) throws CoreException {
    try {
      if (!keyValue.keys().contains(name)) {
        throw new CoreException(
            String.format("A variable with the name '%s' does not exist", name));
      }

      keyValue.delete(name);
    } catch (IOException | JetStreamApiException | InterruptedException e) {
      Thread.currentThread().interrupt();

      throw new CoreException(
          String.format("Failed to delete the variable '%s': %s", name, e.getMessage()));
    }
  }

  /**
   * Returns all context variables.
   *
   * @return Context variables.
   * @throws CoreException If the variables could not be retrieved.
   */
  @Override
  public List<ContextVariable> getAll() throws CoreException {
    var ret = new ArrayList<ContextVariable>();

    try {
      for (var key : knownKeys) {
        var entry = keyValue.get(key);

        // This would be unexpected
        if (entry == null) {
          throw new CoreException(
              String.format("Could not retrieve the value of the variable '%s'", key));
        }

        ret.add(new ContextVariable(entry.getKey(), entry.getValue()));
      }
    } catch (IOException | JetStreamApiException e) {
      throw new RuntimeException(e);
    }

    return ret;
  }

  private byte[] toBytes(Object value) throws CoreException {
    try (var bs = new ByteArrayOutputStream(); var os = new ObjectOutputStream(bs)) {
      // Acquire serialize to bytes
      os.writeObject(value);

      return bs.toByteArray();
    } catch (IOException e) {
      throw new CoreException(
          String.format("Failed to convert object to binary data: %s", e.getMessage()));
    }
  }

  private Object fromBytes(byte[] bytes) throws CoreException {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(
        bytes); ObjectInputStream ois = new ObjectInputStream(bis)) {
      return ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new CoreException(
          String.format("Failed to convert binary data to object", e.getMessage()));
    }
  }
}
