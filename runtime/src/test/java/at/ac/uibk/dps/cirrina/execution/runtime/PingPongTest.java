package at.ac.uibk.dps.cirrina.execution.runtime;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.lang.parser.CollaborativeStateMachineParser;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.runtime.SharedRuntime;
import com.google.common.collect.ArrayListMultimap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class PingPongTest extends RuntimeTest {

  private static CollaborativeStateMachine collaborativeStateMachine;

  @BeforeAll
  public static void setUp() {
    var json = DefaultDescriptions.pingPong;

    var parser = new CollaborativeStateMachineParser();
    Assertions.assertDoesNotThrow(() -> {
      collaborativeStateMachine = CollaborativeStateMachineBuilder.from(parser.parse(json)).build();
    });
  }

  @Test
  public void testPingPongExecute() {
    Assertions.assertDoesNotThrow(() -> {
      final var mockEventHandler = new EventHandler() {

        @Override
        public void close() throws Exception {

        }

        @Override
        public void sendEvent(Event event, String source) {
          propagateEvent(event);
        }

        @Override
        public void subscribe(String topic) {

        }

        @Override
        public void unsubscribe(String topic) {

        }

        @Override
        public void subscribe(String source, String subject) {

        }

        @Override
        public void unsubscribe(String source, String subject) {

        }
      };

      final var mockPersistentContext = new InMemoryContext() {
        private int next = 1;

        @Override
        public void assign(String name, Object value) throws CirrinaException {
          // Don't expect any variables assigned except for v
          assertEquals("v", name);

          // Which is an integer
          assertInstanceOf(Integer.class, value);

          // And should count up to 100
          assertEquals(next++, value);
          assertTrue((Integer) value <= 100);

          super.assign(name, value);
        }
      };

      mockPersistentContext.create("v", 0);

      final var runtime = new SharedRuntime(mockEventHandler, mockPersistentContext, openTelemetry);
      final var serviceImplementationSelector = new ServiceImplementationSelector(ArrayListMultimap.create());

      final var instances = runtime.newInstance(collaborativeStateMachine, serviceImplementationSelector);

      assertEquals(2, instances.size());

      final var instance = runtime.findInstance(instances.getFirst()).get();

      runtime.shutdown(5000);

      assertEquals(100, mockPersistentContext.get("v"));
    });
  }
}
