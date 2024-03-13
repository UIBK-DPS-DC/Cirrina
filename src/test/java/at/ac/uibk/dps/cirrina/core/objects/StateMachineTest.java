package at.ac.uibk.dps.cirrina.core.objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.core.objects.actions.CreateAction;
import at.ac.uibk.dps.cirrina.core.objects.expression.Expression;
import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.lang.checker.Checker;
import at.ac.uibk.dps.cirrina.lang.parser.Parser;
import at.ac.uibk.dps.cirrina.lang.parser.Parser.Options;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StateMachineTest {

  private static StateMachine stateMachine;

  @BeforeAll
  public static void setUp() {
    var json = DefaultDescriptions.complete;

    var parser = new Parser(new Options());
    assertDoesNotThrow(() -> {
      var collaborativeStateMachine = new Checker(new Checker.Options()).check(parser.parse(json));

      stateMachine = collaborativeStateMachine.getStateMachineByName("stateMachine1").get();
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
    assertEquals(stateMachine.getActions().size(), 1);

    var action = stateMachine.getActions().getFirst();
    assertEquals(action.name.get(), "action1");
    assertTrue(action instanceof CreateAction);

    var createAction = (CreateAction) action;
    assertEquals(createAction.variable.name(), "v");
    assertTrue(createAction.variable.isLazy());

    var val = createAction.variable.value();
    assertTrue(val instanceof Expression);

    var expression = (Expression) val;
    assertEquals(expression.source, "5");
  }

  @Test
  public void testGetHandledEvents() {
    var handledEvents = stateMachine.getHandledEvents();
    var expectedHandledEvents = List.of("e1", "e2");
    assertEquals(handledEvents, expectedHandledEvents);
  }

  @Test
  public void testGetRaisedEvents() {
    var raisedEvents = stateMachine.getHandledEvents();
    var expectedRaisedEvents = List.of("e1", "e2");
    assertEquals(raisedEvents, expectedRaisedEvents);
  }

  @Test
  public void testGetStateByNameExists() {
    assertDoesNotThrow(() -> stateMachine.getStateByName("state1"));
    assertDoesNotThrow(() -> stateMachine.getStateByName("state2"));
  }

  @Test
  public void testGetStateByNameDoesNotExist() {
    assertThrows(IllegalArgumentException.class, () -> stateMachine.getStateByName("nonExisting"));
  }

  @Test
  public void testGetActionByNameExists() {
    assertDoesNotThrow(() -> stateMachine.getActionByName("action1"));
  }

  @Test
  public void testGetActionByNameDoesNotExist() {
    assertThrows(IllegalArgumentException.class, () -> stateMachine.getActionByName("nonExisting"));
  }

  @Test
  public void testToString() {
    assertEquals(stateMachine.toString(), "stateMachine1");
  }
}
