package at.ac.uibk.dps.cirrina.core.object.expression;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.jexl3.*;

/**
 * JEXL expression, an expression based on Apache Commons Java Expression Language (JEXL).
 *
 * @see <a href="https://commons.apache.org/proper/commons-jexl/index.html">JEXL Homepage and Documentation</a>
 */
public class JexlExpression extends Expression {

  private static final int CACHE_SIZE = 512; //TODO Determine the maximum amount of cached expressions
  private static final JexlEngine JEXL_ENGINE = getJexlEngine();
  private final JexlScript jexlScript;

  /**
   * Initializes the JEXL expression.
   *
   * @param source Source string.
   */
  JexlExpression(String source) throws IllegalArgumentException {
    super(source);

    try {
      this.jexlScript = JEXL_ENGINE.createScript(source);
    } catch (Exception e) {
      throw new IllegalArgumentException(VerificationException.from(Message.EXPRESSION_COULD_NOT_BE_PARSED, source, e.getMessage()));
    }
  }

  /**
   * Returns the standard JEXL engine.
   *
   * @return JEXL engine.
   */
  private static JexlEngine getJexlEngine() {

    Map<String, Object> namespaces = new HashMap<>();
    namespaces.put("math", Math.class); // Enable math methods, e.g. math:sin(x), math:min(x, y), math:random()

    var features = new JexlFeatures()
        .sideEffectGlobal(false)
        .sideEffect(true);

    return new JexlBuilder()
        .features(features)
        .cache(CACHE_SIZE)
        .namespaces(namespaces)
        .create();
  }

  /**
   * Executes this expression, producing a value.
   *
   * @param extent Extent for resolving variables.
   * @return Result of the expression.
   * @throws CirrinaException In case of an error while executing the expression.
   */
  @Override
  public Object execute(Extent extent) throws CirrinaException {
    try {
      return jexlScript.execute(new ExtentJexlContext(extent));
    } catch (Exception e) {
      throw CirrinaException.from("Could not execute the expression '%s': %s", jexlScript.getSourceText(), e.getMessage());
    }
  }

  /**
   * JEXL context, which has access to all variables within an Extent.
   *
   * @see Extent
   */
  private record ExtentJexlContext(Extent extent) implements JexlContext {

    @Override
    public Object get(String key) {
      return extent.resolve(key).orElseThrow(() -> new NoSuchElementException(String.format("Variable not found: %s", key)));
    }

    @Override
    public void set(String key, Object value) {
    }

    @Override
    public boolean has(String key) {
      return extent.resolve(key).isPresent();
    }
  }
}
