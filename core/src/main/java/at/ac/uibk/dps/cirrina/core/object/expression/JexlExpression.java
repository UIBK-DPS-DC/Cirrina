package at.ac.uibk.dps.cirrina.core.object.expression;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import java.util.NoSuchElementException;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlFeatures;
import org.apache.commons.jexl3.JexlScript;

/**
 * JEXL expression, an expression based on Apache Commons Java Expression Language (JEXL).
 *
 * @see <a href="https://commons.apache.org/proper/commons-jexl/index.html">JEXL Homepage and Documentation</a>
 */
public class JexlExpression extends Expression {

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
    JexlFeatures features = new JexlFeatures()
        .sideEffectGlobal(false)
        .sideEffect(true);

    return new JexlBuilder().features(features).create();
  }

  /**
   * Executes this expression, producing a value.
   *
   * @param extent Extent for resolving variables.
   * @return Result of the expression.
   * @throws RuntimeException In case of an error while executing the expression.
   */
  @Override
  public Object execute(Extent extent) throws RuntimeException {
    try {
      return jexlScript.execute(new ExtentJexlContext(extent));
    } catch (Exception e) {
      throw RuntimeException.from("Could not execute the expression '%s': %s", jexlScript.getSourceText(), e.getMessage());
    }
  }

  /**
     * JEXL context, which has access to all variables within an Extent and a local expression-level context.
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
