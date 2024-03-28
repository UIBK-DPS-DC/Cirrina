package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.core.lang.parser.Parser;
import at.ac.uibk.dps.cirrina.core.lang.parser.Parser.Options;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.runtime.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RoundRobinScheduler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PingPongTest {

  private static StateMachine stateMachine1;
  private static StateMachine stateMachine2;

  @BeforeAll
  public static void setUp() {
    var json = DefaultDescriptions.pingPong;

    var parser = new Parser(new Options());
    Assertions.assertDoesNotThrow(() -> {
      var collaborativeStateMachine = CollaborativeStateMachineBuilder.from(parser.parse(json)).build();

      stateMachine1 = collaborativeStateMachine.getStateMachineByName("stateMachine1").get();
      stateMachine2 = collaborativeStateMachine.getStateMachineByName("stateMachine2").get();
    });
  }

  @Test
  public void testPingPongExecute() {
    Assertions.assertDoesNotThrow(() -> {
      var persistentContext = new InMemoryContext();

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

      var runtime = new Runtime(new RoundRobinScheduler(), mockEventHandler, persistentContext);

      runtime.newInstance(stateMachine1);
      runtime.newInstance(stateMachine2);

      var thread = new Thread(runtime);
      thread.start();
    });
  }
}
