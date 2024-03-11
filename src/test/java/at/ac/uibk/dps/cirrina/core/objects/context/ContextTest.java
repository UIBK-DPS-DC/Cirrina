package at.ac.uibk.dps.cirrina.core.objects.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.uibk.dps.cirrina.core.CoreException;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

public abstract class ContextTest {

  protected abstract Context createContext();

  @Test
  void testCreateAndGetContextVariable() {
    var context = createContext();

    assertDoesNotThrow(() -> context.create("testVar", 42));

    var v = assertDoesNotThrow(() -> context.get("testVar"));
    assertEquals(42, v);
  }

  @Test
  void testCreateDuplicateVariable() {
    var context = createContext();

    assertDoesNotThrow(() -> context.create("testVar", 42));
    assertThrows(CoreException.class, () -> context.create("testVar", 42));
  }

  @Test
  void testGetNonExistentVariable() {
    var context = createContext();

    assertThrows(CoreException.class, () -> context.get("nonExistentVar"));
  }

  @Test
  void testAssign() {
    var context = createContext();

    assertDoesNotThrow(() -> {
      context.create("testVar", 42);
      context.assign("testVar", 100);
    });

    var v = assertDoesNotThrow(() -> context.get("testVar"));
    assertEquals(100, v);
  }

  @Test
  void testAssignNonExistentVariable() {
    var context = createContext();

    assertThrows(CoreException.class, () -> context.assign("nonExistentVar", 1));
  }

  @Test
  void testDelete() {
    var context = createContext();

    assertDoesNotThrow(() -> {
      context.create("testVar", 42);
      context.delete("testVar");
    });

    assertThrows(CoreException.class, () -> context.get("testVar"));
  }

  @Test
  void testDeleteNonExistentVariable() {
    var context = createContext();

    assertThrows(CoreException.class, () -> context.delete("nonExistentVar"));
  }

  @Test
  void testGetAll() {
    var context = createContext();

    assertDoesNotThrow(() -> {
      context.create("var1", 1);
      context.create("var2", "value2");
    });

    var allVariables = assertDoesNotThrow(context::getAll);
    assertEquals(2, allVariables.size());
  }

  @Test
  void testMultiThreadedCreateGet() {
    var context = createContext();

    final int threadCount = 100;
    final int iterationsPerThread = 1000;

    try (var executorService = Executors.newFixedThreadPool(threadCount)) {
      for (int i = 0; i < threadCount; ++i) {
        executorService.submit(() -> {
          assertDoesNotThrow(() -> {
            for (int j = 0; j < iterationsPerThread; ++j) {
              var variableName = Thread.currentThread().threadId() + "_" + j;

              context.create(variableName, j);
              context.get(variableName);
            }
          });
        });
      }
    }

    var allVariables = assertDoesNotThrow(context::getAll);
    assertEquals(threadCount * iterationsPerThread, allVariables.size(),
        "Incorrect number of variables in the context");
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