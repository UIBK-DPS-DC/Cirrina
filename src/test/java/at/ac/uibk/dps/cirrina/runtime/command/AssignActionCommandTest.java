package at.ac.uibk.dps.cirrina.runtime.command;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import at.ac.uibk.dps.cirrina.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.object.context.Extent;
import at.ac.uibk.dps.cirrina.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.object.expression.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.runtime.command.Command.ExecutionContext;
import at.ac.uibk.dps.cirrina.runtime.command.action.AssignActionCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AssignActionCommandTest {

  @Test
  public void testAssignActionCommandPersistentLazy() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    assertDoesNotThrow(() -> {
      persistentContext.create("v", 5);
    });

    var stateMachine = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachine).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("v+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    var assignAction = Mockito.mock(AssignAction.class);
    doReturn(contextVariable).when(assignAction).getVariable();

    var assignActionCommand = new AssignActionCommand(stateMachine, assignAction, false);

    assertDoesNotThrow(() -> {
      assignActionCommand.execute(new ExecutionContext(stateMachine, null));

      assertEquals(persistentContext.get("v"), 6);
    });
  }

  @Test
  public void testAssignActionCommandLocalLazy() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    assertDoesNotThrow(() -> {
      localContext.create("v", 5);
    });

    var stateMachine = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachine).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("v+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    var assignAction = Mockito.mock(AssignAction.class);
    doReturn(contextVariable).when(assignAction).getVariable();

    var assignActionCommand = new AssignActionCommand(stateMachine, assignAction, false);

    assertDoesNotThrow(() -> {
      assignActionCommand.execute(new ExecutionContext(stateMachine, null));

      assertEquals(localContext.get("v"), 6);
    });
  }

  @Test
  public void testAssignActionCommandLocal() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    assertDoesNotThrow(() -> {
      localContext.create("v", 5);
    });

    var stateMachine = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachine).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable).name();
    doReturn("v+1").when(contextVariable).value();
    doReturn(false).when(contextVariable).isLazy();

    var assignAction = Mockito.mock(AssignAction.class);
    doReturn(contextVariable).when(assignAction).getVariable();

    var assignActionCommand = new AssignActionCommand(stateMachine, assignAction, false);

    assertDoesNotThrow(() -> {
      assignActionCommand.execute(new ExecutionContext(stateMachine, null));

      assertEquals(localContext.get("v"), "v+1");
    });
  }

  @Test
  public void testAssignActionCommandOrderLazy() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    assertDoesNotThrow(() -> {
      persistentContext.create("v", 5);
    });
    assertDoesNotThrow(() -> {
      localContext.create("v", 5);
    });

    var stateMachine = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachine).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("v+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    var assignAction = Mockito.mock(AssignAction.class);
    doReturn(contextVariable).when(assignAction).getVariable();

    var assignActionCommand = new AssignActionCommand(stateMachine, assignAction, false);

    assertDoesNotThrow(() -> {
      assignActionCommand.execute(new ExecutionContext(stateMachine, null));

      assertEquals(persistentContext.get("v"), 6);
      assertEquals(localContext.get("v"), 6);
    });
  }
}
