package at.ac.uibk.dps.cirrina.object.expression;

import at.ac.uibk.dps.cirrina.lang.classes.ExpressionClass;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Expression builder, builds an expression based on an expression source string. Built expressions are cached, repeatedly building the same
 * expression will return the same expression.
 */
public final class ExpressionBuilder {

  private static Map<Integer, Expression> cache = new HashMap<>();

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
    // Compute source hash
    var hash = Hashing.sha256().hashString(expressionClass.expression, StandardCharsets.UTF_8).asInt();

    // Construct a new expression if it did not exist in the cache yet
    cache.computeIfAbsent(hash, (h) -> new JexlExpression(expressionClass.expression));

    return cache.get(hash);
  }
}
