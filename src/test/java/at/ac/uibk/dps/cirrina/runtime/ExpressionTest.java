package at.ac.uibk.dps.cirrina.runtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import at.ac.uibk.dps.cirrina.runtime.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.runtime.expression.ExpressionBuilder;
import com.google.protobuf.ByteString;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpressionTest {

  @Test
  public void testExpressionPositive() throws Exception {
    try (var context = new InMemoryContext()) {
      assertDoesNotThrow(() -> {
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

        Assertions.assertEquals(ExpressionBuilder.from("varPlusOneInt+1").build().execute(context), 2L);
        assertEquals(ExpressionBuilder.from("varNegativeOneInt-1").build().execute(context), -2L);
        assertEquals(ExpressionBuilder.from("varPlusOneDouble+1.0").build().execute(context), 2.0);
        assertEquals(ExpressionBuilder.from("varNegativeOneDouble-1.0").build().execute(context),
            -2.0);
        assertEquals(ExpressionBuilder.from("!varTrueBool").build().execute(context), false);
        assertEquals(ExpressionBuilder.from("!varFalseBool").build().execute(context), true);
        assertEquals(ExpressionBuilder.from("varFoobarString").build().execute(context), "foobar");
        assertEquals(ExpressionBuilder.from("varBad1dBytes").build().execute(context), bytes);
        assertEquals(ExpressionBuilder.from("varVariousList").build().execute(context), list);
      });
    }
  }
}
