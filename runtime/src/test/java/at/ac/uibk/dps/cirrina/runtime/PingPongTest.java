package at.ac.uibk.dps.cirrina.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.lang.parser.Parser;
import at.ac.uibk.dps.cirrina.core.lang.parser.Parser.Options;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.runtime.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RoundRobinRuntimeScheduler;
import at.ac.uibk.dps.cirrina.runtime.shared.SharedRuntime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PingPongTest {

  private static CollaborativeStateMachine collaborativeStateMachine;

  @BeforeAll
  public static void setUp() {
    var json = DefaultDescriptions.pingPong;

    var parser = new Parser(new Options());
    Assertions.assertDoesNotThrow(() -> {
      collaborativeStateMachine = CollaborativeStateMachineBuilder.from(parser.parse(json)).build();
    });
  }

  @Test
  public void testPingPongExecute() {
    Assertions.assertDoesNotThrow(() -> {
      var mockEventHandler = new EventHandler() {

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

      // Mock a persistent context using an in-memory context
      var mockPersistentContext = new InMemoryContext() {
        private int next = 1;

        @Override
        public void assign(String name, Object value) throws RuntimeException {
          // Don't expect any variables assigned except for v
          assertEquals("v", name);

          // Which is an integer
          assertTrue(value instanceof Integer);

          // And should count up to 100
          assertEquals(next++, value);
          assertTrue((Integer) value <= 100);

          super.assign(name, value);
        }
      };

      // Create the persistent context variable v
      mockPersistentContext.create("v", 0);

      // Create a runtime
      var runtime = new SharedRuntime(new RoundRobinRuntimeScheduler(), mockEventHandler, mockPersistentContext);

      // Create a new collaborative state machine instance
      runtime.newInstance(collaborativeStateMachine);

      // Run for five seconds
      var thread = new Thread(runtime);
      thread.start();

      thread.join(5000);
    });
  }
}
