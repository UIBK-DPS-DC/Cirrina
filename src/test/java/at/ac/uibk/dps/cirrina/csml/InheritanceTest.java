package at.ac.uibk.dps.cirrina.csml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClass;
import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClassBuilder;
import at.ac.uibk.dps.cirrina.classes.statemachine.StateMachineClass;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription;
import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.execution.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.execution.object.context.Extent;
import at.ac.uibk.dps.cirrina.execution.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.execution.object.expression.Expression;
import at.ac.uibk.dps.cirrina.io.description.DescriptionParser;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class InheritanceTest {

  private CollaborativeStateMachineClass getCollaborativeStateMachine() {
    var parser = new DescriptionParser<CollaborativeStateMachineDescription>(CollaborativeStateMachineDescription.class);
    return assertDoesNotThrow(() -> {
      var csm = parser.parse(DefaultDescriptions.completeInheritance);
      return CollaborativeStateMachineClassBuilder.from(csm).build();
    });
  }

  private void tryCheckCollaborativeStateMachine(String json) {
    var parser = new DescriptionParser<CollaborativeStateMachineDescription>(CollaborativeStateMachineDescription.class);
    var csm = assertDoesNotThrow(() -> parser.parse(json));

    CollaborativeStateMachineClassBuilder.from(csm).build();
  }

  private StateMachineClass getStateMachine1(CollaborativeStateMachineClass csm) {
    return csm.findStateMachineClassByName("stateMachine1")
        .orElseThrow(() -> new AssertionError("stateMachine1 does not exist"));
  }

  private StateMachineClass getStateMachine2(CollaborativeStateMachineClass csm) {
    return csm.findStateMachineClassByName("stateMachine2")
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

    // Test overridden states
    var stateMachine1 = getStateMachine1(csm);
    assertEquals(3, stateMachine1.vertexSet().size());
    var stateMachine1State1 = stateMachine1.findStateClassByName("state1");
    assertTrue(stateMachine1State1.isPresent());
    assertTrue(stateMachine1.findStateClassByName("state2").isPresent());
    assertTrue(stateMachine1.findStateClassByName("state3").isPresent());
    assertTrue(stateMachine1.findStateClassByName("state4").isEmpty());

    var stateMachine2 = getStateMachine2(csm);
    assertEquals(4, stateMachine2.vertexSet().size());
    var stateMachine2State1 = stateMachine2.findStateClassByName("state1");
    assertTrue(stateMachine2State1.isPresent());
    assertTrue(stateMachine2.findStateClassByName("state2").isPresent());
    assertTrue(stateMachine2.findStateClassByName("state3").isPresent());
    assertTrue(stateMachine2.findStateClassByName("state4").isPresent());

    // Test overridden state transitions
    assertEquals(1, stateMachine1.edgeSet().stream()
        .filter(transition -> stateMachine1.getEdgeSource(transition).getName().equals("state1"))
        .count());

    assertEquals(2, stateMachine2.edgeSet().stream()
        .filter(transition -> stateMachine2.getEdgeSource(transition).getName().equals("state1"))
        .count());

    var transitions1 = stateMachine1.findOnTransitionsFromStateByEventName(stateMachine1State1.get(), "e1");
    assertEquals(1, transitions1.size());
    assertEquals("state2", transitions1.getFirst().getTargetStateName());

    var transitions2 = stateMachine2.findOnTransitionsFromStateByEventName(stateMachine2State1.get(), "e1");
    assertEquals(1, transitions2.size());
    assertEquals("state3", transitions2.getFirst().getTargetStateName());
  }

  @Test
  public void testVirtualAndAbstractStates() {
    var csm = getCollaborativeStateMachine();
    assertEquals(2, csm.vertexSet().size());

    var stateMachine1 = getStateMachine1(csm);
    assertDoesNotThrow(() -> {
      var state1 = stateMachine1.findStateClassByName("state1").get();
      assertTrue(state1.isVirtual());
      assertFalse(state1.isAbstract());

      var state2 = stateMachine1.findStateClassByName("state2").get();
      assertFalse(state2.isVirtual());
      assertTrue(state2.isAbstract());
    });

    var stateMachine2 = getStateMachine2(csm);
    assertDoesNotThrow(() -> {
      var state1 = stateMachine2.findStateClassByName("state1").get();
      assertTrue(state1.isVirtual());
      assertFalse(state1.isAbstract());

      var state2 = stateMachine2.findStateClassByName("state2").get();
      assertTrue(state2.isVirtual());
      assertFalse(state2.isAbstract());
    });
  }

  @Test
  public void testMergeAndOverrideContext() {
    var csm = getCollaborativeStateMachine();

    var stateMachine1Context = getStateMachine1(csm).getLocalContextClass();
    assertTrue(stateMachine1Context.isPresent());
    var stateMachine1Variables = stateMachine1Context.get().variables.stream()
        .toList();
    assertEquals(2, stateMachine1Variables.size());
    assertLinesMatch(Stream.of("v1", "v2"), stateMachine1Variables.stream()
        .map(variable -> variable.name));
    assertLinesMatch(Stream.of("0", "0"), stateMachine1Variables.stream()
        .map(variable -> variable.value.expression));

    var stateMachine2Context = getStateMachine2(csm).getLocalContextClass();
    assertTrue(stateMachine2Context.isPresent());
    var stateMachine2Variables = stateMachine2Context.get().variables.stream()
        .toList();
    assertEquals(3, stateMachine2Variables.size());
    assertLinesMatch(Stream.of("v1", "v3", "v2"), stateMachine2Variables.stream()
        .map(variable -> variable.name));
    assertLinesMatch(Stream.of("1", "1", "0"), stateMachine2Variables.stream()
        .map(variable -> variable.value.expression));
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
  public void testMergeAndOverrideGuards() throws Exception {
    var csm = getCollaborativeStateMachine();

    var stateMachine1 = getStateMachine1(csm);
    assertDoesNotThrow(() -> {
      var guard1 = stateMachine1.findGuardByName("guard1").get();
      var guard2 = stateMachine1.findGuardByName("guard2").get();

      assertEquals("true", guard1.getExpression().getSource());
      assertEquals("false", guard2.getExpression().getSource());
    });

    var stateMachine2 = getStateMachine2(csm);
    assertDoesNotThrow(() -> {
      var guard1 = stateMachine2.findGuardByName("guard1").get();
      var guard2 = stateMachine2.findGuardByName("guard2").get();

      assertEquals("true", guard1.getExpression().getSource());
      assertEquals("true", guard2.getExpression().getSource());
    });
  }

  @Test
  public void testInvalidInheritance() {
    assertThrows(IllegalArgumentException.class,
        () -> tryCheckCollaborativeStateMachine(DefaultDescriptions.invalidInheritance));
  }

  @Test
  public void testInvalidStateOverride() {
    assertThrows(IllegalArgumentException.class,
        () -> tryCheckCollaborativeStateMachine(DefaultDescriptions.invalidStateOverride));
  }

  @Test
  public void testInvalidAbstraction() {
    assertThrows(IllegalArgumentException.class,
        () -> tryCheckCollaborativeStateMachine(DefaultDescriptions.invalidAbstraction));
  }

  @Test
  public void testInvalidAbstractStates() {
    assertThrows(IllegalArgumentException.class,
        () -> tryCheckCollaborativeStateMachine(DefaultDescriptions.invalidAbstractStates));
  }
}
