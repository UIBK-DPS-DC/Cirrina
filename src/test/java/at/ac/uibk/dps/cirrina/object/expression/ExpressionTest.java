package at.ac.uibk.dps.cirrina.object.expression;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import at.ac.uibk.dps.cirrina.object.context.Extent;
import at.ac.uibk.dps.cirrina.object.context.InMemoryContext;
import com.google.protobuf.ByteString;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ExpressionTest {

  @Disabled // TODO: Fix resolving of variables
  @Test
  public void testExpressionPositive() throws Exception {
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

        Assertions.assertEquals(ExpressionBuilder.from("varPlusOneInt+1").build().execute(extent), 2L);
        assertEquals(ExpressionBuilder.from("varNegativeOneInt-1").build().execute(extent), -2L);
        assertEquals(ExpressionBuilder.from("varPlusOneDouble+1.0").build().execute(extent), 2.0);
        assertEquals(ExpressionBuilder.from("varNegativeOneDouble-1.0").build().execute(extent),
            -2.0);
        assertEquals(ExpressionBuilder.from("!varTrueBool").build().execute(extent), false);
        assertEquals(ExpressionBuilder.from("!varFalseBool").build().execute(extent), true);
        assertEquals(ExpressionBuilder.from("varFoobarString").build().execute(extent), "foobar");
        assertEquals(ExpressionBuilder.from("varBad1dBytes").build().execute(extent), bytes);
        assertEquals(ExpressionBuilder.from("varVariousList").build().execute(extent), list);
      });
    }
  }
}
