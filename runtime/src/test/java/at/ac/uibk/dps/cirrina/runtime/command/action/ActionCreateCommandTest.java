package at.ac.uibk.dps.cirrina.runtime.command.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.core.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.expression.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import at.ac.uibk.dps.cirrina.execution.command.ExecutionContext;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ActionCreateCommandTest {

  @Test
  public void testCreateActionCommandPersistentLazy() {
    final var persistentContext = new InMemoryContext();
    final var localContext = new InMemoryContext();

    final var stateMachineInstance = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachineInstance).getExtent();

    final var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("var").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("5+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    final var createAction = Mockito.mock(CreateAction.class);
    doReturn(true).when(createAction).isPersistent();
    doReturn(contextVariable).when(createAction).getVariable();

    final var executionContext = new ExecutionContext(stateMachineInstance, null, null, null, null, null, false);

    final var commandFactory = new CommandFactory(executionContext);

    final var createActionCommand = commandFactory.createActionCommand(createAction);

    assertDoesNotThrow(() -> {
      createActionCommand.execute();

      var value = persistentContext.get("var");
      assertEquals(value, 6);
    });
  }

  @Test
  public void testCreateActionCommandLocalLazy() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    final var stateMachineInstance = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachineInstance).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("var").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("5+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    var createAction = Mockito.mock(CreateAction.class);
    doReturn(false).when(createAction).isPersistent();
    doReturn(contextVariable).when(createAction).getVariable();

    final var executionContext = new ExecutionContext(stateMachineInstance, null, null, null, null, null, false);

    final var commandFactory = new CommandFactory(executionContext);

    final var createActionCommand = commandFactory.createActionCommand(createAction);

    assertDoesNotThrow(() -> {
      createActionCommand.execute();

      var value = localContext.get("var");
      assertEquals(value, 6);
    });
  }

  @Test
  public void testCreateActionCommandLocal() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    final var stateMachineInstance = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachineInstance).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("var").when(contextVariable).name();
    doReturn("5+1").when(contextVariable).value();
    doReturn(false).when(contextVariable).isLazy();

    var createAction = Mockito.mock(CreateAction.class);
    doReturn(false).when(createAction).isPersistent();
    doReturn(contextVariable).when(createAction).getVariable();

    final var executionContext = new ExecutionContext(stateMachineInstance, null, null, null, null, null, false);

    final var commandFactory = new CommandFactory(executionContext);

    final var createActionCommand = commandFactory.createActionCommand(createAction);

    assertDoesNotThrow(() -> {
      createActionCommand.execute();

      var value = localContext.get("var");
      assertEquals(value, "5+1");
    });
  }

  @Test
  public void testCreateActionCommandDuplicateLazy() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    final var stateMachineInstance = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachineInstance).getExtent();

    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("var").when(contextVariable).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("5+1")).build()).when(contextVariable).value();
    doReturn(true).when(contextVariable).isLazy();

    var createAction = Mockito.mock(CreateAction.class);
    doReturn(false).when(createAction).isPersistent();
    doReturn(contextVariable).when(createAction).getVariable();

    final var executionContext = new ExecutionContext(stateMachineInstance, null, null, null, null, null, false);

    final var commandFactory = new CommandFactory(executionContext);

    final var createActionCommand = commandFactory.createActionCommand(createAction);

    assertThrows(CirrinaException.class, () -> {
      createActionCommand.execute();

      var value = localContext.get("var");
      assertEquals(value, 6);

      createActionCommand.execute();
    });
  }
}
