package at.ac.uibk.dps.cirrina.core.objects;

import at.ac.uibk.dps.cirrina.core.objects.builder.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.core.objects.context.InMemoryContext;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionTest {

    @Test
    public void testExpressionPositive() {
        var builder = new ExpressionBuilder();
        var context = new InMemoryContext();

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

            assertEquals(builder.build("varPlusOneInt+1").execute(context), 2L);
            assertEquals(builder.build("varNegativeOneInt-1").execute(context), -2L);
            assertEquals(builder.build("varPlusOneDouble+1.0").execute(context), 2.0);
            assertEquals(builder.build("varNegativeOneDouble-1.0").execute(context), -2.0);
            assertEquals(builder.build("!varTrueBool").execute(context), false);
            assertEquals(builder.build("!varFalseBool").execute(context), true);
            assertEquals(builder.build("varFoobarString").execute(context), "foobar");
            assertEquals(builder.build("varBad1dBytes").execute(context), bytes);
            assertEquals(builder.build("varVariousList").execute(context), list);
        });
    }
}
