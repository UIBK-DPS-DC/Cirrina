package at.ac.uibk.dps.cirrina.core.object.context;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
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
  // TODO: Make default
  public InMemoryContext() {
  }

  /**
   * Retrieve a context variable.
   *
   * @param name Name of the context variable.
   * @return The retrieved context variable.
   * @throws CirrinaException If the context variable could not be retrieved.
   */
  @Override
  public Object get(String name) throws CirrinaException {
    if (!values.containsKey(name)) {
      throw CirrinaException.from("A variable with the name '%s' does not exist", name);
    }

    return values.get(name);
  }

  /**
   * Creates a context variable.
   *
   * @param name  Name of the context variable.
   * @param value Value of the context variable.
   * @throws CirrinaException If the variable could not be created.
   */
  @Override
  public void create(String name, Object value) throws CirrinaException {
    if (values.containsKey(name)) {
      throw CirrinaException.from("A variable with the name '%s' already exists", name);
    }

    values.put(name, value);
  }

  /**
   * Assigns to a context variable.
   *
   * @param name  Name of the context variable.
   * @param value New value of the context variable.
   * @throws CirrinaException If the variable could not be assigned to.
   */
  @Override
  public void assign(String name, Object value) throws CirrinaException {
    if (!values.containsKey(name)) {
      throw CirrinaException.from("A variable with the name '%s' does not exist", name);
    }

    values.put(name, value);
  }

  /**
   * Deletes a context variable.
   *
   * @param name Name of the context variable.
   * @throws CirrinaException If the variable could not be deleted.
   */
  @Override
  public void delete(String name) throws CirrinaException {
    if (!values.containsKey(name)) {
      throw CirrinaException.from("A variable with the name '%s' does not exist", name);
    }

    values.remove(name);
  }

  /**
   * Returns all context variables.
   *
   * @return Context variables.
   */
  @Override
  public List<ContextVariable> getAll() throws CirrinaException {
    return values.entrySet().stream()
        .map(entry -> new ContextVariable(entry.getKey(), entry.getValue()))
        .toList();
  }

  @Override
  public void close() throws Exception {

  }
}
