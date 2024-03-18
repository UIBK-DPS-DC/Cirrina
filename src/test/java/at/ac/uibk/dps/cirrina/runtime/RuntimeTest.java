package at.ac.uibk.dps.cirrina.runtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.lang.parser.Parser;
import at.ac.uibk.dps.cirrina.lang.parser.Parser.Options;
import at.ac.uibk.dps.cirrina.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import at.ac.uibk.dps.cirrina.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RoundRobinScheduler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RuntimeTest {

  private static StateMachine stateMachine;

  @BeforeAll
  public static void setUp() {
    var json = DefaultDescriptions.complete;

    var parser = new Parser(new Options());
    assertDoesNotThrow(() -> {
      var collaborativeStateMachine = CollaborativeStateMachineBuilder.from(parser.parse(json)).build();

      stateMachine = collaborativeStateMachine.getStateMachineByName("stateMachine1").get();
    });
  }

  @Test
  public void testEnterInitialState() {
    assertDoesNotThrow(() -> {
      var runtime = new Runtime<RoundRobinScheduler>(RoundRobinScheduler.class);

      var thread = new Thread(runtime);
      thread.start();

      runtime.newInstance(stateMachine);
    });
  }
}
