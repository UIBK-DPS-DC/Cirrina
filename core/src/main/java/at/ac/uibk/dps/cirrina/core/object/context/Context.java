package at.ac.uibk.dps.cirrina.core.object.context;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
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
   * @throws CirrinaException If the context variable could not be retrieved.
   */
  public abstract Object get(String name) throws CirrinaException;

  /**
   * Creates a context variable.
   *
   * @param name  Name of the context variable.
   * @param value Value of the context variable.
   * @throws CirrinaException If the variable could not be created.
   */
  public abstract void create(String name, Object value) throws CirrinaException;

  /**
   * Assigns to a context variable.
   *
   * @param name  Name of the context variable.
   * @param value New value of the context variable.
   * @throws CirrinaException If the variable could not be assigned to.
   */
  public abstract void assign(String name, Object value) throws CirrinaException;

  /**
   * Deletes a context variable.
   *
   * @param name Name of the context variable.
   * @throws CirrinaException If the variable could not be deleted.
   */
  public abstract void delete(String name) throws CirrinaException;

  /**
   * Returns all context variables.
   *
   * @return Context variables.
   */
  public abstract List<ContextVariable> getAll() throws CirrinaException;
}
