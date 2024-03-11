package at.ac.uibk.dps.cirrina.core.objects.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.uibk.dps.cirrina.core.CoreException;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class ContextTest {

  @Test
  void testCreateAndGetContextVariable() {
    var context = new InMemoryContext();

    assertDoesNotThrow(() -> {
      context.create("testVar", 42);

      assertEquals(42, context.get("testVar"));
    });
  }

  @Test
  void testCreateDuplicateVariable() {
    var context = new InMemoryContext();

    assertDoesNotThrow(() -> context.create("testVar", 42));
    assertThrows(CoreException.class, () -> context.create("testVar", 42));
  }

  @Test
  void testGetNonExistentVariable() {
    var context = new InMemoryContext();

    assertThrows(CoreException.class, () -> context.get("nonExistentVar"));
  }

  @Test
  void testSync() {
    var context = new InMemoryContext();

    assertDoesNotThrow(() -> {
      context.create("testVar", 42);
      context.assign("testVar", 100);

      assertEquals(100, context.get("testVar"));
    });
  }

  @Test
  void testGetAll() {
    var context = new InMemoryContext();

    assertDoesNotThrow(() -> {
      context.create("var1", 1);
      context.create("var2", "value2");

      var allVariables = context.getAll();

      assertEquals(2, allVariables.size());
    });
  }

  @Test
  void testMultiThreadedCreateGet() {
    final int threadCount = 100;
    final int iterationsPerThread = 1000;

    var context = new InMemoryContext();

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
  }

  @Test
  void testMultiThreadedSetValueGetValue() {
    assertDoesNotThrow(() -> {
      final int threadCount = 100;
      final int iterationsPerThread = 1000;

      var variableName = "testVar";

      var context = new InMemoryContext();

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