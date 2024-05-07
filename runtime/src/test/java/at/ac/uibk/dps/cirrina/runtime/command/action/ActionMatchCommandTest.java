package at.ac.uibk.dps.cirrina.runtime.command.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.core.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.core.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.expression.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import at.ac.uibk.dps.cirrina.execution.command.ExecutionContext;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ActionMatchCommandTest {

  /**
   * Match action actionCommand test, evaluates the correct execution of a matching case action in a match action.
   */
  @Test
  public void testMatchActionCommand() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    final var stateMachineInstance = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachineInstance).getExtent();

    // Create a local variable 'v' with value 5
    assertDoesNotThrow(() -> {
      localContext.create("v", 5);
    });

    // Create a mock context variable containing the expression 'v+1'
    var contextVariable1 = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable1).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("v+1")).build()).when(contextVariable1).value();
    doReturn(true).when(contextVariable1).isLazy();

    // Create a mock assignment action that performs 'v=v+1'
    var assignAction1 = Mockito.mock(AssignAction.class);
    doReturn(contextVariable1).when(assignAction1).getVariable();

    // Create a mock context variable containing the expression 'v+1'
    var contextVariable2 = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable2).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("v+2")).build()).when(contextVariable2).value();
    doReturn(true).when(contextVariable2).isLazy();

    // Create a mock assignment action that performs 'v=v+2'
    var assignAction2 = Mockito.mock(AssignAction.class);
    doReturn(contextVariable2).when(assignAction2).getVariable();

    // Create a mock match action that:
    // Executes 'v=v+1' when 'v' is 5
    // Executes 'v=v+2' when 'v' is 6
    var matchAction = Mockito.mock(MatchAction.class);
    doReturn(ExpressionBuilder.from(new ExpressionClass("v")).build()).when(matchAction).getValue();
    doReturn(Map.of(
        ExpressionBuilder.from(new ExpressionClass("5")).build(), assignAction1,
        ExpressionBuilder.from(new ExpressionClass("6")).build(), assignAction2)).when(matchAction).getCase();

    final var executionContext = new ExecutionContext(
        stateMachineInstance,
        null,
        null,
        null,
        false
    );

    final var commandFactory = new CommandFactory(executionContext);

    final var matchActionCommand = commandFactory.createActionCommand(matchAction);

    // Execute the match action actionCommand, expect to get 6 as 'v' is 5 and we execute the matching action that performs the assignment 'v=v+1'
    assertDoesNotThrow(() -> {
      for (var command : matchActionCommand.execute()) {
        command.execute();
      }

      assertEquals(6, localContext.get("v"));
    });
  }
}
