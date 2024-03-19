package at.ac.uibk.dps.cirrina.lang.checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.exception.VerificationException;
import at.ac.uibk.dps.cirrina.exception.VerificationException.Message;
import at.ac.uibk.dps.cirrina.lang.parser.Parser;
import at.ac.uibk.dps.cirrina.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import at.ac.uibk.dps.cirrina.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.object.statemachine.StateMachine;
import java.util.List;
import org.junit.jupiter.api.Test;

public class InheritanceTest {

  private CollaborativeStateMachine getCollaborativeStateMachine() {

    var parser = new Parser(new Parser.Options());
    return assertDoesNotThrow(() -> {
      var csm = parser.parse(DefaultDescriptions.completeInheritance);
      return CollaborativeStateMachineBuilder.from(csm).build();
    });
  }

  private CollaborativeStateMachine getCollaborativeStateMachineUnchecked(String json) throws VerificationException {
    var parser = new Parser(new Parser.Options());
    var csm = assertDoesNotThrow(() -> parser.parse(json));

    return CollaborativeStateMachineBuilder.from(csm).build();
  }

  private StateMachine getStateMachine1(CollaborativeStateMachine csm) {
    return csm.getStateMachineByName("stateMachine1")
        .orElseThrow(() -> new AssertionError("stateMachine1 does not exist"));
  }

  private StateMachine getStateMachine2(CollaborativeStateMachine csm) {
    return csm.getStateMachineByName("stateMachine2")
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

  @Test
  public void testMergeAndOverrideStates() {
    var csm = getCollaborativeStateMachine();
    assertEquals(2, csm.vertexSet().size());

    var stateMachine1 = getStateMachine1(csm);
    assertEquals(3, stateMachine1.vertexSet().size());
    assertDoesNotThrow(() -> {
      var state1 = stateMachine1.getStateByName("state1");
      var state2 = stateMachine1.getStateByName("state2");
      var state3 = stateMachine1.getStateByName("state3");
    });
    assertThrows(IllegalArgumentException.class,
        () -> stateMachine1.getStateByName("state4"));

    var stateMachine2 = getStateMachine2(csm);
    assertEquals(4, stateMachine2.vertexSet().size());
    assertDoesNotThrow(() -> {
      var state1 = stateMachine2.getStateByName("state1");
      var state2 = stateMachine2.getStateByName("state2");
      var state3 = stateMachine2.getStateByName("state3");
      var state4 = stateMachine2.getStateByName("state4");
    });
  }

  @Test
  public void testVirtualAndAbstractStates() {
    var csm = getCollaborativeStateMachine();
    assertEquals(2, csm.vertexSet().size());

    var stateMachine1 = getStateMachine1(csm);
    assertDoesNotThrow(() -> {
      var state1 = stateMachine1.getStateByName("state1");
      assertTrue(state1.isVirtual());
      assertFalse(state1.isAbstract());

      var state2 = stateMachine1.getStateByName("state2");
      assertFalse(state2.isVirtual());
      assertTrue(state2.isAbstract());
    });

    var stateMachine2 = getStateMachine2(csm);
    assertDoesNotThrow(() -> {
      var state1 = stateMachine2.getStateByName("state1");
      assertTrue(state1.isVirtual());
      assertFalse(state1.isAbstract());

      var state2 = stateMachine2.getStateByName("state2");
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

  @Test
  public void testMergeAndOverrideActions() throws Exception {
    var csm = getCollaborativeStateMachine();
    try (var context = new InMemoryContext()) {
      var stateMachine1 = getStateMachine1(csm);
      assertDoesNotThrow(() -> {
        var action1 = stateMachine1.getActionByName("action1");
        var action2 = stateMachine1.getActionByName("action2");
        assertInstanceOf(AssignAction.class, action1);
        assertInstanceOf(AssignAction.class, action2);
        assertEquals(0L, ((AssignAction) action1).value.execute(context));
        assertEquals(1L, ((AssignAction) action2).value.execute(context));
      });

      var stateMachine2 = getStateMachine2(csm);
      assertDoesNotThrow(() -> {
        var action1 = stateMachine2.getActionByName("action1");
        var action2 = stateMachine2.getActionByName("action2");
        assertInstanceOf(AssignAction.class, action1);
        assertInstanceOf(AssignAction.class, action2);
        assertEquals(0L, ((AssignAction) action1).value.execute(context));
        assertEquals(2L, ((AssignAction) action2).value.execute(context));
      });
    }
  }

  @Test
  public void testInvalidInheritance() {
    VerificationException exception = assertThrows(VerificationException.class,
        () -> getCollaborativeStateMachineUnchecked(DefaultDescriptions.invalidInheritance));
    assertEquals(exception.message, Message.STATE_MACHINE_INHERITS_FROM_INVALID);
  }

  @Test
  public void testInvalidStateOverride() {
    VerificationException exception = assertThrows(VerificationException.class,
        () -> getCollaborativeStateMachineUnchecked(DefaultDescriptions.invalidStateOverride));
    assertEquals(exception.message, Message.STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES);
  }

  @Test
  public void testInvalidAbstraction() {
    VerificationException exception = assertThrows(VerificationException.class,
        () -> getCollaborativeStateMachineUnchecked(DefaultDescriptions.invalidAbstraction));
    assertEquals(exception.message, Message.STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES);
  }

  @Test
  public void testInvalidAbstractStates() {
    VerificationException exception = assertThrows(VerificationException.class,
        () -> getCollaborativeStateMachineUnchecked(DefaultDescriptions.invalidAbstractStates));
    assertEquals(exception.message, Message.NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES);
  }
}