package at.ac.uibk.dps.cirrina.execution.object.context;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory context, where context variables are contained in a hash map.
 */
public class InMemoryContext extends Context {

  private final Map<String, Object> values = new ConcurrentHashMap<>();

  /**
   * Initializes an empty in-memory context.
   */
  public InMemoryContext() {
  }

  /**
   * Retrieve a context variable.
   *
   * @param name Name of the context variable.
   * @return The retrieved context variable.
   * @throws IOException If a variable with the same does not exist.
   * @throws IOException If the context variable could not be retrieved.
   */
  @Override
  public Object get(String name) throws IOException {
    if (!values.containsKey(name)) {
      throw new IOException("A variable with the name '%s' does not exist".formatted(name));
    }

    return values.get(name);
  }

  /**
   * Creates a context variable.
   *
   * @param name  Name of the context variable.
   * @param value Value of the context variable.
   * @throws IOException If a variable with the same name already exists.
   * @throws IOException If the variable could not be created.
   */
  @Override
  public void create(String name, Object value) throws IOException {
    if (values.containsKey(name)) {
      throw new IOException("A variable with the name '%s' already exists".formatted(name));
    }

    values.put(name, value);
  }

  /**
   * Assigns to a context variable.
   *
   * @param name  Name of the context variable.
   * @param value New value of the context variable.
   * @throws IOException If a variable with the same does not exist.
   * @throws IOException If the variable could not be assigned to.
   */
  @Override
  public void assign(String name, Object value) throws IOException {
    if (!values.containsKey(name)) {
      throw new IOException("A variable with the name '%s' does not exist".formatted(name));
    }

    values.put(name, value);
  }

  /**
   * Deletes a context variable.
   *
   * @param name Name of the context variable.
   * @throws IOException If a variable with the same does not exist.
   * @throws IOException If the variable could not be deleted.
   */
  @Override
  public void delete(String name) throws IOException {
    if (!values.containsKey(name)) {
      throw new IOException("A variable with the name '%s' does not exist".formatted(name));
    }

    values.remove(name);
  }

  /**
   * Returns all context variables.
   *
   * @return Context variables.
   * @throws IOException If the variables could not be retrieved.
   */
  @Override
  public List<ContextVariable> getAll() throws IOException {
    return values.entrySet().stream()
        .map(entry -> new ContextVariable(entry.getKey(), entry.getValue()))
        .toList();
  }

  @Override
  public void close() throws IOException {

  }
}
