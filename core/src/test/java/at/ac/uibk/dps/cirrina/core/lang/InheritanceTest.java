package at.ac.uibk.dps.cirrina.core.lang;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.core.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message;
import at.ac.uibk.dps.cirrina.core.lang.parser.Parser;
import at.ac.uibk.dps.cirrina.core.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.expression.Expression;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class InheritanceTest {

  private CollaborativeStateMachine getCollaborativeStateMachine() {

    var parser = new Parser(new Parser.Options());
    return assertDoesNotThrow(() -> {
      var csm = parser.parse(DefaultDescriptions.completeInheritance);
      return CollaborativeStateMachineBuilder.from(csm).build();
    });
  }

  private void tryCheckCollaborativeStateMachine(String json) throws VerificationException {

    var parser = new Parser(new Parser.Options());
    var csm = assertDoesNotThrow(() -> parser.parse(json));

    CollaborativeStateMachineBuilder.from(csm).build();
  }

  private StateMachine getStateMachine1(CollaborativeStateMachine csm) {
    return csm.findStateMachineByName("stateMachine1")
        .orElseThrow(() -> new AssertionError("stateMachine1 does not exist"));
  }

  private StateMachine getStateMachine2(CollaborativeStateMachine csm) {
    return csm.findStateMachineByName("stateMachine2")
        .orElseThrow(() -> new AssertionError("stateMachine2 does not exist"));
  }

  @Test
  public void testDoesNotOverrideAbstractFlag() {
    var csm = getCollaborativeStateMachine();

    var stateMachine1 = getStateMachine1(csm);
    assertTrue(stateMachine1.isAbstract());

    var stateMachine2 = getStateMachine2(csm);
    assertFalse(stateMachine2.isAbstract());
  }

  @Disabled
  @Test
  public void testMergeAndOverrideStates() {
    var csm = getCollaborativeStateMachine();
    assertEquals(2, csm.vertexSet().size());

    var stateMachine1 = getStateMachine1(csm);
    assertEquals(3, stateMachine1.vertexSet().size());
    assertDoesNotThrow(() -> {
      stateMachine1.findStateByName("state1");
      stateMachine1.findStateByName("state2");
      stateMachine1.findStateByName("state3");
    });
    assertThrows(IllegalArgumentException.class,
        () -> stateMachine1.findStateByName("state4"));

    var stateMachine2 = getStateMachine2(csm);
    assertEquals(4, stateMachine2.vertexSet().size());
    assertDoesNotThrow(() -> {
      stateMachine2.findStateByName("state1");
      stateMachine2.findStateByName("state2");
      stateMachine2.findStateByName("state3");
      stateMachine2.findStateByName("state4");
    });
  }

  @Test
  public void testVirtualAndAbstractStates() {
    var csm = getCollaborativeStateMachine();
    assertEquals(2, csm.vertexSet().size());

    var stateMachine1 = getStateMachine1(csm);
    assertDoesNotThrow(() -> {
      var state1 = stateMachine1.findStateByName("state1").get();
      assertTrue(state1.isVirtual());
      assertFalse(state1.isAbstract());

      var state2 = stateMachine1.findStateByName("state2").get();
      assertFalse(state2.isVirtual());
      assertTrue(state2.isAbstract());
    });

    var stateMachine2 = getStateMachine2(csm);
    assertDoesNotThrow(() -> {
      var state1 = stateMachine2.findStateByName("state1").get();
      assertTrue(state1.isVirtual());
      assertFalse(state1.isAbstract());

      var state2 = stateMachine2.findStateByName("state2").get();
      assertTrue(state2.isVirtual());
      assertFalse(state2.isAbstract());
    });
  }

  @Test
  public void testMergeHandledEvents() {
    var csm = getCollaborativeStateMachine();

    var stateMachine1 = getStateMachine1(csm);
    assertLinesMatch(List.of("e1"), stateMachine1.getInputEvents());

    var stateMachine2 = getStateMachine2(csm);
    assertLinesMatch(List.of("e1", "e2", "e3", "e4"), stateMachine2.getInputEvents());
  }

  @Disabled
  @Test
  public void testMergeAndOverrideActions() throws Exception {
    var csm = getCollaborativeStateMachine();
    try (var context = new InMemoryContext()) {
      var stateMachine1 = getStateMachine1(csm);
      assertDoesNotThrow(() -> {
        var extent = new Extent(context);

        var action1 = stateMachine1.findActionByName("action1").get();
        var action2 = stateMachine1.findActionByName("action2").get();

        assertInstanceOf(AssignAction.class, action1);
        assertInstanceOf(AssignAction.class, action2);
        assertEquals(0, ((Expression) ((AssignAction) action1).getVariable().value()).execute(extent));
        assertEquals(1, ((Expression) ((AssignAction) action2).getVariable().value()).execute(extent));
      });

      var stateMachine2 = getStateMachine2(csm);
      assertDoesNotThrow(() -> {
        var extent = new Extent(context);

        var action1 = stateMachine2.findActionByName("action1").get();
        var action2 = stateMachine2.findActionByName("action2").get();

        assertInstanceOf(AssignAction.class, action1);
        assertInstanceOf(AssignAction.class, action2);
        assertEquals(0, ((Expression) ((AssignAction) action1).getVariable().value()).execute(extent));
        assertEquals(2, ((Expression) ((AssignAction) action2).getVariable().value()).execute(extent));
      });
    }
  }

  @Test
  public void testInvalidInheritance() {
    VerificationException exception = assertThrows(VerificationException.class,
        () -> tryCheckCollaborativeStateMachine(DefaultDescriptions.invalidInheritance));
    assertEquals(Message.STATE_MACHINE_INHERITS_FROM_INVALID, exception.message);
  }

  @Test
  public void testInvalidStateOverride() {
    VerificationException exception = assertThrows(VerificationException.class,
        () -> tryCheckCollaborativeStateMachine(DefaultDescriptions.invalidStateOverride));
    assertEquals(Message.STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES, exception.message);
  }

  @Test
  public void testInvalidAbstraction() {
    VerificationException exception = assertThrows(VerificationException.class,
        () -> tryCheckCollaborativeStateMachine(DefaultDescriptions.invalidAbstraction));
    assertEquals(Message.STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES, exception.message);
  }

  @Test
  public void testInvalidAbstractStates() {
    VerificationException exception = assertThrows(VerificationException.class,
        () -> tryCheckCollaborativeStateMachine(DefaultDescriptions.invalidAbstractStates));
    assertEquals(Message.NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES, exception.message);
  }
}
