package at.ac.uibk.dps.cirrina.core.objects.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.uibk.dps.cirrina.core.CoreException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class ContextTest {

  @Test
  void testCreateAndGetContextVariable() throws CoreException {
    var context = new InMemoryContext();
    var variable = context.create("testVar", 42);

    assertNotNull(variable);
    assertEquals("testVar", variable.name);
    assertEquals(42, variable.getValue());
    assertEquals(variable, context.get("testVar"));
  }

  @Test
  void testCreateDuplicateVariable() {
    Context context = new InMemoryContext();

    assertDoesNotThrow(() -> context.create("testVar", 42));
    assertThrows(CoreException.class, () -> context.create("testVar", 42));
  }

  @Test
  void testGetNonExistentVariable() {
    Context context = new InMemoryContext();

    assertThrows(CoreException.class, () -> context.get("nonExistentVar"));
  }

  @Test
  void testSync() throws CoreException {
    var context = new InMemoryContext();
    var variable = context.create("testVar", 42);

    variable.setValue(100);
    assertEquals(100, variable.getValue());
  }

  @Test
  void testGetAll() throws CoreException {
    var context = new InMemoryContext();
    context.create("var1", 1);
    context.create("var2", "value2");

    var allVariables = context.getAll();
    assertEquals(2, allVariables.size());
  }

  @Test
  void testMultiThreadedCreateGet() throws InterruptedException {
    final int threadCount = 100;
    final int iterationsPerThread = 1000;

    Context context = new InMemoryContext();

    try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
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

    List<Context.ContextVariable> allVariables = context.getAll();
    assertEquals(threadCount * iterationsPerThread, allVariables.size(),
        "Incorrect number of variables in the context");
  }

  @Test
  void testMultiThreadedSetValueGetValue() throws InterruptedException {
    assertDoesNotThrow(() -> {
      final int threadCount = 100;
      final int iterationsPerThread = 1000;

      String variableName = "testVar";
      Context context = new InMemoryContext();
      context.create(variableName, 0);

      try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
        for (int i = 0; i < threadCount; ++i) {
          executorService.submit(() -> {
            assertDoesNotThrow(() -> {
              for (int j = 0; j < iterationsPerThread; ++j) {
                context.get(variableName).setValue(j);
              }
            });
          });
        }
      }

      // Check that the final value is equal to the last value set
      assertEquals(iterationsPerThread - 1, (int) context.get(variableName).getValue(),
          "Incorrect final value after multi-threaded setValue");
    });
  }
}