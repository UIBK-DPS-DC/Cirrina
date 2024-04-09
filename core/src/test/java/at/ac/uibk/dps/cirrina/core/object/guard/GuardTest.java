package at.ac.uibk.dps.cirrina.core.object.guard;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.guard.GuardClass;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class GuardTest {

  @Test
  public void testGuard() throws Exception {
    try (var context = new InMemoryContext()) {
      context.create("v", 5);

      var extent = new Extent(context);

      assertDoesNotThrow(() -> {
        var guardClass = new GuardClass();

        guardClass.name = Optional.of("name");
        guardClass.expression = new ExpressionClass("v==5");

        var guard = GuardBuilder.from(guardClass).build();

        assertEquals("name", guard.getName().get());
        assertTrue(guard.evaluate(extent));

        guardClass.expression = new ExpressionClass("v==6");

        guard = GuardBuilder.from(guardClass).build();

        assertFalse(guard.evaluate(extent));
      });

      assertThrows(RuntimeException.class, () -> {
        var guardClass = new GuardClass();

        guardClass.name = Optional.of("name");
        guardClass.expression = new ExpressionClass("v");

        var guard = GuardBuilder.from(guardClass).build();

        guard.evaluate(extent);
      });
    }
  }
}
