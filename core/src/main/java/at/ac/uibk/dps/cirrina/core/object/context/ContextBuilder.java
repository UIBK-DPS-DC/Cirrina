package at.ac.uibk.dps.cirrina.core.object.context;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.lang.classes.context.ContextClass;
import at.ac.uibk.dps.cirrina.core.object.expression.ExpressionBuilder;
import java.util.Optional;

/**
 * Context builder, builder for various context implementations.
 */
public class ContextBuilder {

  private final Optional<ContextClass> contextClass;

  private Context context;

  /**
   * Initializes this context builder object.
   */
  private ContextBuilder() {
    this.contextClass = Optional.empty();
  }

  /**
   * Initializes this context builder object.
   *
   * @param contextClass Context class.
   */
  private ContextBuilder(ContextClass contextClass) {
    this.contextClass = Optional.of(contextClass);
  }

  /**
   * Construct a builder from nothing.
   *
   * @return Context builder.
   */
  public static ContextBuilder from() {
    return new ContextBuilder();
  }

  /**
   * Construct a builder from a context class.
   *
   * @param contextClass Context class.
   * @return Context builder.
   */
  public static ContextBuilder from(ContextClass contextClass) {
    return new ContextBuilder(contextClass);
  }

  /**
   * Build an in-memory context.
   *
   * @return This builder.
   */
  public ContextBuilder inMemoryContext() {
    context = new InMemoryContext();

    return this;
  }

  /**
   * Build a NATS context.
   *
   * @param natsUrl    NATS url.
   * @param bucketName NATS bucket name.
   * @return This builder.
   * @throws CirrinaException
   * @see NatsContext
   */
  public ContextBuilder natsContext(String natsUrl, String bucketName) throws CirrinaException {
    context = new NatsContext(natsUrl, bucketName);

    return this;
  }

  /**
   * Builds the current context.
   *
   * @return Context.
   * @throws CirrinaException
   */
  public Context build() throws CirrinaException {
    // TODO: No CirrinaException. Probably best to remove all types of exceptions and just have one anyways...

    assert context != null;

    // Add all variables contained within the context class to the newly created context, only do this if there is a class
    if (contextClass.isPresent()) {
      var contextClass = this.contextClass.get();

      for (var contextVariable : contextClass.variables) {
        // Build the value expression
        var expression = ExpressionBuilder.from(contextVariable.value).build();

        // Acquire the variable name
        var name = contextVariable.name;

        // Acquire the variable value
        // We pass an empty extent here, I don't think that it makes too much sense to provide anything other than an empty extent here,
        // because I currently don't see a use case for looking up variables in scope while constructing a context
        var value = expression.execute(new Extent());

        // Create the variable
        context.create(name, value);
      }
    }

    return context;
  }
}
