package at.ac.uibk.dps.cirrina.nats;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.uibk.dps.cirrina.core.CoreException;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class NatsPersistentContextTest {

  private NatsPersistentContext getNatsPersistentContext() throws CoreException {
    String natsServerURL = System.getenv("NATS_SERVER_URL");

    Assumptions.assumeFalse(natsServerURL == null);

    return new NatsPersistentContext(natsServerURL);
  }

  @Test
  void testCreateAndGetContextVariable() {
    assertDoesNotThrow(() -> {
      var context = getNatsPersistentContext();

      assertDoesNotThrow(() -> {
        context.create("testVar", 42);

        assertEquals(42, context.get("testVar"));
      });
    });
  }

  @Test
  void testCreateDuplicateVariable() {
    assertDoesNotThrow(() -> {
      var context = getNatsPersistentContext();

      assertDoesNotThrow(() -> context.create("testVar", 42));
      assertThrows(CoreException.class, () -> context.create("testVar", 42));
    });
  }

  @Test
  void testGetNonExistentVariable() {
    assertDoesNotThrow(() -> {
      var context = getNatsPersistentContext();

      assertThrows(CoreException.class, () -> context.get("nonExistentVar"));
    });
  }

  @Test
  void testSync() {
    assertDoesNotThrow(() -> {
      var context = getNatsPersistentContext();

      assertDoesNotThrow(() -> {
        context.create("testVar", 42);
        context.assign("testVar", 100);

        assertEquals(100, context.get("testVar"));
      });
    });
  }

  @Test
  void testGetAll() {
    assertDoesNotThrow(() -> {
      var context = getNatsPersistentContext();

      assertDoesNotThrow(() -> {
        context.create("var1", 1);
        context.create("var2", "value2");

        var allVariables = context.getAll();

        assertEquals(2, allVariables.size());
      });
    });
  }

  @Test
  void testMultiThreadedCreateGet() {
    final int threadCount = 100;
    final int iterationsPerThread = 1000;

    assertDoesNotThrow(() -> {
      var context = getNatsPersistentContext();

      try (var executorService = Executors.newFixedThreadPool(threadCount)) {
        for (int i = 0; i < threadCount; ++i) {
          executorService.submit(() -> {
            assertDoesNotThrow(() -> {
              for (int j = 0; j < iterationsPerThread; ++j) {
                var variableName = Thread.currentThread().getId() + "_" + j;

                context.create(variableName, j);
                context.get(variableName);
              }
            });
          });
        }
      }

      assertDoesNotThrow(() -> {
        var allVariables = context.getAll();
        assertEquals(threadCount * iterationsPerThread, allVariables.size(),
            "Incorrect number of variables in the context");
      });
    });
  }

  @Test
  void testMultiThreadedSetValueGetValue() {
    assertDoesNotThrow(() -> {
      final int threadCount = 100;
      final int iterationsPerThread = 1000;

      var variableName = "testVar";

      var context = getNatsPersistentContext();

      context.create(variableName, 0);

      try (var executorService = Executors.newFixedThreadPool(threadCount)) {
        for (int i = 0; i < threadCount; ++i) {
          executorService.submit(() -> {
            assertDoesNotThrow(() -> {
              for (int j = 0; j < iterationsPerThread; ++j) {
                context.assign(variableName, j);
              }
            });
          });
        }
      }

      // Check that the final value is equal to the last value set
      assertEquals(iterationsPerThread - 1, (int) context.get(variableName),
          "Incorrect final value after multi-threaded setValue");
    });
  }
}
