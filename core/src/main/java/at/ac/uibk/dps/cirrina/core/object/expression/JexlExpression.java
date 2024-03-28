package at.ac.uibk.dps.cirrina.core.object.expression;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlFeatures;
import org.apache.commons.jexl3.JexlScript;

public class JexlExpression extends Expression {

  private final JexlScript jexlScript;

  /**
   * Initializes the JEXL expression.
   *
   * @param source Source string.
   */
  JexlExpression(String source) {
    super(source);
    this.jexlScript = getJexlEngine().createScript(source);
  }

  private JexlEngine getJexlEngine() {
    JexlFeatures features = new JexlFeatures()
        .sideEffectGlobal(false)
        .sideEffect(false);

    return new JexlBuilder().features(features).create();
  }

  @Override
  public Object execute(Extent extent) throws RuntimeException {
    return jexlScript.execute(new ExtentContext(extent));
  }

  private record ExtentContext(Extent extent) implements JexlContext {

    @Override
    public Object get(String key) {
      return extent.resolve(key).orElse(null);
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
