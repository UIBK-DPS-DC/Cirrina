package at.ac.uibk.dps.cirrina.core.object.expression;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ExpressionTest {

  @Test
  public void testExpression() throws Exception {
    try (var context = new InMemoryContext()) {
      assertDoesNotThrow(() -> {
        var extent = new Extent(context);

        var bytes = new byte[4];
        ByteBuffer.wrap(bytes).putInt(0xBAD1D);
        var list = List.of(-1, 1, true, "foobar");

        context.create("varPlusOneInt", +1);
        context.create("varNegativeOneInt", -1);
        context.create("varPlusOneDouble", +1.0);
        context.create("varNegativeOneDouble", -1.0);
        context.create("varTrueBool", true);
        context.create("varFalseBool", false);
        context.create("varFoobarString", "foobar");
        context.create("varBad1dBytes", bytes);
        context.create("varVariousList", list);

        assertEquals(2, ExpressionBuilder.from(new ExpressionClass("varPlusOneInt+1")).build().execute(extent));
        assertEquals(-2, ExpressionBuilder.from(new ExpressionClass("varNegativeOneInt-1")).build().execute(extent));
        assertEquals(2.0, ExpressionBuilder.from(new ExpressionClass("varPlusOneDouble+1.0")).build().execute(extent));
        assertEquals(-2.0, ExpressionBuilder.from(new ExpressionClass("varNegativeOneDouble-1.0")).build().execute(extent));
        assertEquals(false, ExpressionBuilder.from(new ExpressionClass("!varTrueBool")).build().execute(extent));
        assertEquals(true, ExpressionBuilder.from(new ExpressionClass("!varFalseBool")).build().execute(extent));
        assertEquals("foobar", ExpressionBuilder.from(new ExpressionClass("varFoobarString")).build().execute(extent));
        assertEquals(bytes, ExpressionBuilder.from(new ExpressionClass("varBad1dBytes")).build().execute(extent));
        assertEquals(list, ExpressionBuilder.from(new ExpressionClass("varVariousList")).build().execute(extent));
      });
    }
  }

  @Test
  public void testMultiLineExpression() throws Exception {
    try (var context = new InMemoryContext()) {
      assertDoesNotThrow(() -> {
        var extent = new Extent(context);

        context.create("varOneInt", 1);

        var multiLineExpression =
            new ExpressionClass("let varExpressionLocal = 1; varExpressionLocal += varOneInt; varExpressionLocal");
        assertEquals(2, ExpressionBuilder.from(multiLineExpression).build().execute(extent));
      });
    }
  }

  @Test
  public void testExpressionUsingNamespace() throws Exception {
    try (var context = new InMemoryContext()) {
      assertEquals(1, ExpressionBuilder.from(new ExpressionClass("math:abs(-1)")).build().execute(new Extent(context)));
    }
  }

  @Test
  public void testExpressionNegative() throws Exception {
    try (var context = new InMemoryContext()) {
      var extent = new Extent(context);

      context.create("varOneInt", 1);

      // Throws while parsing
      assertThrows(IllegalArgumentException.class,
          () -> ExpressionBuilder.from(new ExpressionClass("1 + ")).build().execute(extent));
      assertThrows(IllegalArgumentException.class,
          () -> ExpressionBuilder.from(new ExpressionClass("varOneInt = 2")).build().execute(extent));

      // Throws at runtime
      assertThrows(RuntimeException.class,
          () -> ExpressionBuilder.from(new ExpressionClass("varInvalid")).build().execute(extent));
      assertThrows(RuntimeException.class,
          () -> ExpressionBuilder.from(new ExpressionClass("!varInvalid")).build().execute(extent));
      assertThrows(RuntimeException.class,
          () -> ExpressionBuilder.from(new ExpressionClass("varInvalid.varInvalidSub")).build().execute(extent));
      assertThrows(RuntimeException.class,
          () -> ExpressionBuilder.from(new ExpressionClass("varInvalid + 1")).build().execute(extent));
      assertThrows(RuntimeException.class,
          () -> ExpressionBuilder.from(new ExpressionClass("let varTemp = varInvalid; varTemp")).build().execute(extent));
    }
  }
}
