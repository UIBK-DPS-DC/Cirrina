package at.ac.uibk.dps.cirrina.classes.statemachine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClassBuilder;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription;
import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.execution.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.execution.object.expression.Expression;
import at.ac.uibk.dps.cirrina.io.description.DescriptionParser;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StateMachineClassTest {

  private static StateMachineClass stateMachineClass;

  @BeforeAll
  public static void setUp() {
    var json = DefaultDescriptions.complete;

    var parser = new DescriptionParser<CollaborativeStateMachineDescription>(CollaborativeStateMachineDescription.class);
    assertDoesNotThrow(() -> {
      var collaborativeStateMachine = CollaborativeStateMachineClassBuilder.from(parser.parse(json)).build();

      stateMachineClass = collaborativeStateMachine.findStateMachineClassByName("stateMachine1").get();
    });
  }

  @Test
  public void testGetName() {
    assertEquals(stateMachineClass.getName(), "stateMachine1");
  }

  @Test
  public void testGetActions() {
    assertEquals(stateMachineClass.getNamedActions().size(), 1);

    var action = stateMachineClass.getNamedActions().getFirst();
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
    var handledEvents = stateMachineClass.getInputEvents();
    var expectedHandledEvents = List.of("e1", "e2");
    assertEquals(handledEvents, expectedHandledEvents);
  }

  @Test
  public void testGetRaisedEvents() {
    var raisedEvents = stateMachineClass.getInputEvents();
    var expectedRaisedEvents = List.of("e1", "e2");
    assertEquals(raisedEvents, expectedRaisedEvents);
  }

  @Test
  public void testGetStateByName() {
    assertDoesNotThrow(() -> stateMachineClass.findStateClassByName("state1"));
    assertDoesNotThrow(() -> stateMachineClass.findStateClassByName("state2"));

    assertTrue(stateMachineClass.findStateClassByName("nonExisting").isEmpty());
  }

  @Test
  public void testGetActionByName() {
    assertDoesNotThrow(() -> stateMachineClass.findStateClassByName("action1"));

    assertTrue(stateMachineClass.findStateClassByName("nonExisting").isEmpty());
  }

  @Test
  public void testFindStateByName() {
    assertDoesNotThrow(() -> {
      stateMachineClass.findStateClassByName("state1").get().getName().equals("state1");

      assertFalse(stateMachineClass.findStateClassByName("nonExisting").isPresent());
    });
  }

  @Test
  public void testFindTransitionByEventName() {
    assertDoesNotThrow(() -> {
      var t = stateMachineClass.findOnTransitionsFromStateByEventName(stateMachineClass.findStateClassByName("state1").get(), "e1");
      assertEquals(1, t.size());
      assertEquals("state2", t.getFirst().getTargetStateName().get());

      assertEquals(0,
          stateMachineClass.findOnTransitionsFromStateByEventName(stateMachineClass.findStateClassByName("state1").get(), "nonExisting")
              .size());
    });
  }

  @Test
  public void testToString() {
    assertEquals(stateMachineClass.toString(), "stateMachine1");
  }
}
