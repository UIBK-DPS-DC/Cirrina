package at.ac.uibk.dps.cirrina.core.object.expression;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import com.google.protobuf.ByteString;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ExpressionTest {

  @Test
  public void testExpression() throws Exception {
    try (var context = new InMemoryContext()) {
      assertDoesNotThrow(() -> {
        var extent = new Extent(context);

        var bytes = ByteString.copyFrom(ByteBuffer.allocate(4).putInt(0xBAD1D).array());
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

        assertEquals(ExpressionBuilder.from(new ExpressionClass("varPlusOneInt+1")).build().execute(extent), 2);
        assertEquals(ExpressionBuilder.from(new ExpressionClass("varNegativeOneInt-1")).build().execute(extent), -2);
        assertEquals(ExpressionBuilder.from(new ExpressionClass("varPlusOneDouble+1.0")).build().execute(extent), 2.0);
        assertEquals(ExpressionBuilder.from(new ExpressionClass("varNegativeOneDouble-1.0")).build().execute(extent),
            -2.0);
        assertEquals(ExpressionBuilder.from(new ExpressionClass("!varTrueBool")).build().execute(extent), false);
        assertEquals(ExpressionBuilder.from(new ExpressionClass("!varFalseBool")).build().execute(extent), true);
        assertEquals(ExpressionBuilder.from(new ExpressionClass("varFoobarString")).build().execute(extent), "foobar");
        assertEquals(ExpressionBuilder.from(new ExpressionClass("varBad1dBytes")).build().execute(extent), bytes);
        assertEquals(ExpressionBuilder.from(new ExpressionClass("varVariousList")).build().execute(extent), list);
      });
    }
  }
}
