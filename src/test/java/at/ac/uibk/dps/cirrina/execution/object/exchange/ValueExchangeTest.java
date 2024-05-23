package at.ac.uibk.dps.cirrina.execution.object.exchange;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ValueExchangeTest {

  @Test
  public void testToFromBytes() {
    final var i = 1;
    final var f = 1.0f;
    final var l = 1L;
    final var d = 1.0;
    final var s = "1";
    final var b = true;

    assertDoesNotThrow(() -> {
      assertEquals(i, ValueExchange.fromBytes(new ValueExchange(i).toBytes()).getValue());
      assertEquals(f, ValueExchange.fromBytes(new ValueExchange(f).toBytes()).getValue());
      assertEquals(l, ValueExchange.fromBytes(new ValueExchange(l).toBytes()).getValue());
      assertEquals(d, ValueExchange.fromBytes(new ValueExchange(d).toBytes()).getValue());
      assertEquals(s, ValueExchange.fromBytes(new ValueExchange(s).toBytes()).getValue());
      assertEquals(b, ValueExchange.fromBytes(new ValueExchange(b).toBytes()).getValue());
    });
  }
}