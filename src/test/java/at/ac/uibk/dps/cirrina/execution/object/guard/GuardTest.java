package at.ac.uibk.dps.cirrina.execution.object.guard;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.csml.description.ExpressionDescription;
import at.ac.uibk.dps.cirrina.csml.description.guard.GuardDescription;
import at.ac.uibk.dps.cirrina.execution.object.context.Extent;
import at.ac.uibk.dps.cirrina.execution.object.context.InMemoryContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class GuardTest {

  @Test
  public void testGuard() throws Exception {
    try (var context = new InMemoryContext(true)) {
      context.create("v", 5);

      var extent = new Extent(context);

      assertDoesNotThrow(() -> {
        var guardClass = new GuardDescription();

        guardClass.name = Optional.of("name");
        guardClass.expression = new ExpressionDescription("v==5");

        var guard = GuardBuilder.from(guardClass).build();

        assertEquals("name", guard.getName().get());
        assertTrue(guard.evaluate(extent));

        guardClass.expression = new ExpressionDescription("v==6");

        guard = GuardBuilder.from(guardClass).build();

        assertFalse(guard.evaluate(extent));
      });

      assertThrows(IllegalArgumentException.class, () -> {
        var guardClass = new GuardDescription();

        guardClass.name = Optional.of("name");
        guardClass.expression = new ExpressionDescription("v");

        var guard = GuardBuilder.from(guardClass).build();

        guard.evaluate(extent);
      });
    }
  }
}
