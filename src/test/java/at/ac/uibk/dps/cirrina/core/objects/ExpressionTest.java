package at.ac.uibk.dps.cirrina.core.objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import at.ac.uibk.dps.cirrina.core.objects.builder.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.core.objects.context.InMemoryContext;
import org.junit.jupiter.api.Test;

public class ExpressionTest {

  @Test
  public void testExpressionPositive() {
    var builder = new ExpressionBuilder();
    var context = new InMemoryContext();

    assertDoesNotThrow(() -> {
      context.create("v", 0);

      var result = builder.build("v+1").execute(context);

      System.out.println(result);
    });
  }
}
