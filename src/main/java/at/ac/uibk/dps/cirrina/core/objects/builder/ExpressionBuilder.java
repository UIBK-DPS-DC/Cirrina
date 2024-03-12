package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.objects.expression.CelExpression;
import at.ac.uibk.dps.cirrina.core.objects.expression.Expression;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Expression builder, builds an expression based on an expression source string. Built expressions
 * are cached, repeatedly building the same expression will return the same expression.
 */
public class ExpressionBuilder {

  private static Map<Integer, Expression> cache = new HashMap<>();

  /**
   * Builds the collaborative state machine.
   *
   * @param source The expression source string.
   * @return Built expression.
   * @throws IllegalArgumentException In case the expression could not be built.
   */
  public static Expression build(String source)
      throws IllegalArgumentException {
    // Compute source hash
    var hash = Hashing.sha256()
        .hashString(source, StandardCharsets.UTF_8).asInt();

    // Construct a new expression if it did not exist in the cache yet
    cache.computeIfAbsent(hash, (h) -> new CelExpression(source));

    return cache.get(hash);
  }
}
