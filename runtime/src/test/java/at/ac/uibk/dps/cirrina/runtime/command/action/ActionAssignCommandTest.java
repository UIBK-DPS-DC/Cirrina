package at.ac.uibk.dps.cirrina.runtime.command.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.core.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.expression.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import at.ac.uibk.dps.cirrina.execution.command.ExecutionContext;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ActionAssignCommandTest {

  @Test
  public void testAssignActionCommandPersistentLazy() {
    final var persistentContext = new InMemoryContext();
    final var localContext = new InMemoryContext();

    assertDoesNotThrow(() -> {
      persistentContext.create("v", 5);
    });

    final var stateMachineInstance = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachineInstance).getExtent();

    final var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("v+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    final var assignAction = Mockito.mock(AssignAction.class);
    doReturn(contextVariable).when(assignAction).getVariable();

    final var executionContext = new ExecutionContext(stateMachineInstance, null, null, null, false);

    final var commandFactory = new CommandFactory(executionContext);

    final var assignActionCommand = commandFactory.createActionCommand(assignAction);

    assertDoesNotThrow(() -> {
      assignActionCommand.execute();

      assertEquals(6, persistentContext.get("v"));
    });
  }

  @Test
  public void testAssignActionCommandLocalLazy() {
    final var persistentContext = new InMemoryContext();
    final var localContext = new InMemoryContext();

    assertDoesNotThrow(() -> {
      localContext.create("v", 5);
    });

    final var stateMachineInstance = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachineInstance).getExtent();

    final var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("v+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    final var assignAction = Mockito.mock(AssignAction.class);
    doReturn(contextVariable).when(assignAction).getVariable();

    final var executionContext = new ExecutionContext(stateMachineInstance, null, null, null, false);

    final var commandFactory = new CommandFactory(executionContext);

    final var assignActionCommand = commandFactory.createActionCommand(assignAction);

    assertDoesNotThrow(() -> {
      assignActionCommand.execute();

      assertEquals(6, localContext.get("v"));
    });
  }

  @Test
  public void testAssignActionCommandLocal() {
    final var persistentContext = new InMemoryContext();
    final var localContext = new InMemoryContext();

    assertDoesNotThrow(() -> {
      localContext.create("v", 5);
    });

    final var stateMachineInstance = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachineInstance).getExtent();

    final var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable).name();
    doReturn("v+1").when(contextVariable).value();
    doReturn(false).when(contextVariable).isLazy();

    final var assignAction = Mockito.mock(AssignAction.class);
    doReturn(contextVariable).when(assignAction).getVariable();

    final var executionContext = new ExecutionContext(stateMachineInstance, null, null, null, false);

    final var commandFactory = new CommandFactory(executionContext);

    final var assignActionCommand = commandFactory.createActionCommand(assignAction);

    assertDoesNotThrow(() -> {
      assignActionCommand.execute();

      assertEquals("v+1", localContext.get("v"));
    });

  }

  @Test
  public void testAssignActionCommandOrderLazy() {
    final var persistentContext = new InMemoryContext();
    final var localContext = new InMemoryContext();

    assertDoesNotThrow(() -> {
      persistentContext.create("v", 5);
    });
    assertDoesNotThrow(() -> {
      localContext.create("v", 5);
    });

    final var stateMachineInstance = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachineInstance).getExtent();

    final var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("v+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    final var assignAction = Mockito.mock(AssignAction.class);
    doReturn(contextVariable).when(assignAction).getVariable();

    final var executionContext = new ExecutionContext(stateMachineInstance, null, null, null, false);

    final var commandFactory = new CommandFactory(executionContext);

    final var assignActionCommand = commandFactory.createActionCommand(assignAction);

    assertDoesNotThrow(() -> {
      assignActionCommand.execute();

      assertEquals(persistentContext.get("v"), 6);
      assertEquals(localContext.get("v"), 6);
    });
  }
}
