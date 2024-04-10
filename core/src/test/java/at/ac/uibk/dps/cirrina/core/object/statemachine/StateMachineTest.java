package at.ac.uibk.dps.cirrina.core.object.statemachine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.core.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.core.lang.parser.Parser;
import at.ac.uibk.dps.cirrina.core.lang.parser.Parser.Options;
import at.ac.uibk.dps.cirrina.core.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import at.ac.uibk.dps.cirrina.core.object.expression.Expression;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StateMachineTest {

  private static StateMachine stateMachine;

  @BeforeAll
  public static void setUp() {
    var json = DefaultDescriptions.complete;

    var parser = new Parser(new Options());
    assertDoesNotThrow(() -> {
      var collaborativeStateMachine = CollaborativeStateMachineBuilder.from(parser.parse(json)).build();

      stateMachine = collaborativeStateMachine.findStateMachineByName("stateMachine1").get();
    });
  }

  @Test
  public void testGetName() {
    assertEquals(stateMachine.getName(), "stateMachine1");
  }

  @Test
  public void testIsAbstract() {
    assertEquals(stateMachine.isAbstract(), false);
  }

  @Test
  public void testGetActions() {
    assertEquals(stateMachine.getNamedActions().size(), 1);

    var action = stateMachine.getNamedActions().getFirst();
    Assertions.assertEquals(action.getName().get(), "action1");
    assertTrue(action instanceof CreateAction);

    var createAction = (CreateAction) action;
    Assertions.assertEquals(createAction.getVariable().name(), "v");
    Assertions.assertTrue(createAction.getVariable().isLazy());

    var val = createAction.getVariable().value();
    assertTrue(val instanceof Expression);

    var expression = (Expression) val;
    assertEquals(expression.getSource(), "5");
  }

  @Test
  public void testGetHandledEvents() {
    var handledEvents = stateMachine.getInputEvents();
    var expectedHandledEvents = List.of("e1", "e2");
    assertEquals(handledEvents, expectedHandledEvents);
  }

  @Test
  public void testGetRaisedEvents() {
    var raisedEvents = stateMachine.getInputEvents();
    var expectedRaisedEvents = List.of("e1", "e2");
    assertEquals(raisedEvents, expectedRaisedEvents);
  }

  @Test
  public void testGetStateByName() {
    assertDoesNotThrow(() -> stateMachine.findStateByName("state1"));
    assertDoesNotThrow(() -> stateMachine.findStateByName("state2"));

    assertTrue(stateMachine.findStateByName("nonExisting").isEmpty());
  }

  @Test
  public void testGetActionByName() {
    assertDoesNotThrow(() -> stateMachine.findStateByName("action1"));

    assertTrue(stateMachine.findStateByName("nonExisting").isEmpty());
  }

  @Test
  public void testFindStateByName() {
    assertDoesNotThrow(() -> {
      stateMachine.findStateByName("state1").get().getName().equals("state1");

      assertFalse(stateMachine.findStateByName("nonExisting").isPresent());
    });
  }

  public void testFindTransitionByEventName() {
    assertDoesNotThrow(() -> {
      var t = stateMachine.findOnTransitionsFromStateByEventName(stateMachine.findStateByName("state1").get(), "e1");
      assertEquals(1, t.size());
      assertEquals("state2", t.getFirst().getTarget());

      assertEquals(0,
          stateMachine.findOnTransitionsFromStateByEventName(stateMachine.findStateByName("state1").get(), "nonExisting").size());
    });
  }

  @Test
  public void testToString() {
    assertEquals(stateMachine.toString(), "stateMachine1");
  }
}
