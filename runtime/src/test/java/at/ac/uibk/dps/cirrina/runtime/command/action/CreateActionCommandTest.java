package at.ac.uibk.dps.cirrina.runtime.command.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.core.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.expression.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.runtime.command.Command.ExecutionContext;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CreateActionCommandTest {

  @Test
  public void testCreateActionCommandPersistentLazy() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    var stateMachine = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachine).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("var").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("5+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    var createAction = Mockito.mock(CreateAction.class);
    doReturn(true).when(createAction).isPersistent();
    doReturn(contextVariable).when(createAction).getVariable();

    var createActionCommand = new CreateActionCommand(stateMachine, createAction, false);

    assertDoesNotThrow(() -> {
      createActionCommand.execute(new ExecutionContext(stateMachine, null));

      var value = persistentContext.get("var");
      assertEquals(value, 6);
    });
  }

  @Test
  public void testCreateActionCommandLocalLazy() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    var stateMachine = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachine).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("var").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("5+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    var createAction = Mockito.mock(CreateAction.class);
    doReturn(false).when(createAction).isPersistent();
    doReturn(contextVariable).when(createAction).getVariable();

    var createActionCommand = new CreateActionCommand(stateMachine, createAction, false);

    assertDoesNotThrow(() -> {
      createActionCommand.execute(new ExecutionContext(stateMachine, null));

      var value = localContext.get("var");
      assertEquals(value, 6);
    });
  }

  @Test
  public void testCreateActionCommandLocal() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    var stateMachine = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachine).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("var").when(contextVariable).name();
    doReturn("5+1").when(contextVariable).value();
    doReturn(false).when(contextVariable).isLazy();

    var createAction = Mockito.mock(CreateAction.class);
    doReturn(false).when(createAction).isPersistent();
    doReturn(contextVariable).when(createAction).getVariable();

    var createActionCommand = new CreateActionCommand(stateMachine, createAction, false);

    assertDoesNotThrow(() -> {
      createActionCommand.execute(new ExecutionContext(stateMachine, null));

      var value = localContext.get("var");
      assertEquals(value, "5+1");
    });
  }

  @Test
  public void testCreateActionCommandDuplicateLazy() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    var stateMachine = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachine).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("var").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("5+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    var createAction = Mockito.mock(CreateAction.class);
    doReturn(false).when(createAction).isPersistent();
    doReturn(contextVariable).when(createAction).getVariable();

    var createActionCommand = new CreateActionCommand(stateMachine, createAction, false);

    assertThrows(RuntimeException.class, () -> {
      createActionCommand.execute(new ExecutionContext(stateMachine, null));

      var value = localContext.get("var");
      assertEquals(value, 6);

      createActionCommand.execute(new ExecutionContext(stateMachine, null));
    });
  }
}
