package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.objects.context.Context.ContextVariable;
import at.ac.uibk.dps.cirrina.lang.parser.classes.context.ContextVariableClass;

/**
 * Context variable builder, used to build context variable objects.
 */
public class ContextVariableBuilder {

  /**
   * The context variable class to build from.
   */
  private final ContextVariableClass contextVariableClass;

  /**
   * Initializes a context variable builder.
   *
   * @param contextVariableClass Context variable class.
   */
  private ContextVariableBuilder(ContextVariableClass contextVariableClass) {
    this.contextVariableClass = contextVariableClass;
  }

  /**
   * Creates a context variable builder.
   *
   * @param contextVariableClass Context variable class.
   * @return Context variable builder.
   */
  public static ContextVariableBuilder from(ContextVariableClass contextVariableClass) {
    return new ContextVariableBuilder(contextVariableClass);
  }

  /**
   * Builds the context variable.
   *
   * @return The built context variable.
   */
  public ContextVariable build() {
    // If the variable comes from a context variable class, the variable is always lazy as the value is an expression
    var lazyVariable = ExpressionBuilder.from(contextVariableClass.value.expression).build();
    return new ContextVariable(
        contextVariableClass.name,
        lazyVariable
    );
  }
}
