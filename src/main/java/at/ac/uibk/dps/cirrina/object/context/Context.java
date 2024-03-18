package at.ac.uibk.dps.cirrina.object.context;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.expression.Expression;
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

  /**
   * Context variable, contained within a context.
   */
  public record ContextVariable(
      /**
       * Name of the context variable.
       */
      String name,

      /**
       * Value of the context variable. Can be an arbitrary value or must be an expression in case this context variable is lazy.
       */
      Object value,

      /**
       * Laziness of the context variable, if true, the value must be an expression and the value of the context variable is acquired through executing the expression.
       */
      boolean isLazy
  ) {

    /**
     * Initializes a lazy context variable.
     *
     * @param name  Name of the variable.
     * @param value Value expression of the variable.
     */
    ContextVariable(String name, Expression value) {
      this(name, value, true);
    }

    /**
     * Initializes a non-lazy context variable.
     *
     * @param name  Name of the variable.
     * @param value Value of the variable.
     */
    ContextVariable(String name, Object value) {
      this(name, value, false);
    }
  }
}
