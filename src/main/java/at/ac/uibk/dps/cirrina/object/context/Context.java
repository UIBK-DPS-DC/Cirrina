package at.ac.uibk.dps.cirrina.object.context;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import java.util.List;

/**
 * Base context, containing context variables.
 */
public abstract class Context implements AutoCloseable {

  /**
   * Retrieve a context variable.
   *
   * @param name Name of the context variable.
   * @return The retrieved context variable.
   * @throws RuntimeException If the context variable could not be retrieved.
   */
  public abstract Object get(String name) throws RuntimeException;

  /**
   * Creates a context variable.
   *
   * @param name  Name of the context variable.
   * @param value Value of the context variable.
   * @throws RuntimeException If the variable could not be created.
   */
  public abstract void create(String name, Object value) throws RuntimeException;

  /**
   * Assigns to a context variable.
   *
   * @param name  Name of the context variable.
   * @param value New value of the context variable.
   * @throws RuntimeException If the variable could not be assigned to.
   */
  public abstract void assign(String name, Object value) throws RuntimeException;

  /**
   * Deletes a context variable.
   *
   * @param name Name of the context variable.
   * @throws RuntimeException If the variable could not be deleted.
   */
  public abstract void delete(String name) throws RuntimeException;

  /**
   * Returns all context variables.
   *
   * @return Context variables.
   */
  public abstract List<ContextVariable> getAll() throws RuntimeException;
}
