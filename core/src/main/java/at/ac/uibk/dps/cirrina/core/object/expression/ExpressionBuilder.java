package at.ac.uibk.dps.cirrina.core.object.expression;

import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Expression builder, builds an expression based on an expression source string. Built expressions are cached, repeatedly building the same
 * expression will return the same expression.
 */
public final class ExpressionBuilder {

  private ExpressionClass expressionClass;

  private ExpressionBuilder(ExpressionClass expressionClass) {
    this.expressionClass = expressionClass;
  }

  public static ExpressionBuilder from(ExpressionClass expressionClass) {
    return new ExpressionBuilder(expressionClass);
  }

  /**
   * Builds the collaborative state machine.
   *
   * @return Built expression.
   * @throws IllegalArgumentException In case the expression could not be built.
   */
  public Expression build() throws IllegalArgumentException {
    return new JexlExpression(expressionClass.expression);
  }
}
