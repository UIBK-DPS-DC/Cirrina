package at.ac.uibk.dps.cirrina.lang.checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.core.objects.StateMachine;
import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.actions.AssignAction;
import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.lang.parser.Parser;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CheckerTest {

  @Test
  public void testCheckerPositive() {
    var json = DefaultDescriptions.complete;

    var parser = new Parser(new Parser.Options());
    assertDoesNotThrow(() -> {
      var csm = parser.parse(json);

      var checker = new Checker(new Checker.Options());
      checker.check(csm);
    });
  }

  @Test
  public void testCheckerInheritance() {
    var json = DefaultDescriptions.completeInheritance;

    var parser = new Parser(new Parser.Options());
    var csmChecked = assertDoesNotThrow(() -> {
      var csm = parser.parse(json);

      var checker = new Checker(new Checker.Options());
      return checker.check(csm);
    });

    //TODO Refactor and cleanup. Add more assertions for inheritance rules. Maybe split into multiple tests for each.

    assertEquals(2, csmChecked.vertexSet().size());

    StateMachine stateMachine1 = csmChecked.getStateMachineByName("stateMachine1")
        .orElseThrow(() -> new AssertionError("stateMachine1 does not exist"));
    assertTrue(stateMachine1.isAbstract());
    assertLinesMatch(List.of("e1"), stateMachine1.getHandledEvents());
    assertDoesNotThrow(() -> {
      Action action1 = stateMachine1.getActionByName("action1");
      Action action2 = stateMachine1.getActionByName("action2");
      assertInstanceOf(AssignAction.class, action1);
      assertInstanceOf(AssignAction.class, action2);
    });

    StateMachine stateMachine2 = csmChecked.getStateMachineByName("stateMachine2")
        .orElseThrow(() -> new AssertionError("stateMachine2 does not exist"));
    assertFalse(stateMachine2.isAbstract());
    assertLinesMatch(List.of("e1", "e2", "e3"), stateMachine2.getHandledEvents());
    assertDoesNotThrow(() -> {
      Action action1 = stateMachine2.getActionByName("action1");
      Action action2 = stateMachine2.getActionByName("action2");
      assertInstanceOf(AssignAction.class, action1);
      assertInstanceOf(AssignAction.class, action2);
    });
  }
}
