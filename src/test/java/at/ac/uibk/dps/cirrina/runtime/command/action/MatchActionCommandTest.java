package at.ac.uibk.dps.cirrina.runtime.command.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.object.context.Extent;
import at.ac.uibk.dps.cirrina.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.object.event.Event;
import at.ac.uibk.dps.cirrina.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.object.expression.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.runtime.command.Command.ExecutionContext;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class MatchActionCommandTest {

  @Test
  public void testMatchActionCommand() {
    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    assertDoesNotThrow(() -> {
      localContext.create("v", 5);
    });

    var eventHandler = new EventHandler() {

      @Override
      public void close() throws Exception {

      }

      @Override
      public void sendEvent(Event event, String source) throws RuntimeException {

      }

      @Override
      public void subscribe(String subject) {

      }

      @Override
      public void unsubscribe(String subject) {

      }

      @Override
      public void subscribe(String source, String subject) {

      }

      @Override
      public void unsubscribe(String source, String subject) {

      }
    };

    var stateMachine = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachine).getExtent();

    var contextVariable1 = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable1).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("v+1")).build()).when(contextVariable1).value();
    doReturn(true).when(contextVariable1).isLazy();

    var assignAction1 = Mockito.mock(AssignAction.class);
    doReturn(contextVariable1).when(assignAction1).getVariable();

    var contextVariable2 = Mockito.mock(ContextVariable.class);
    doReturn("v").when(contextVariable2).name();
    doReturn(ExpressionBuilder.from(new ExpressionClass("v+2")).build()).when(contextVariable2).value();
    doReturn(true).when(contextVariable2).isLazy();

    var assignAction2 = Mockito.mock(AssignAction.class);
    doReturn(contextVariable2).when(assignAction2).getVariable();

    var matchAction = Mockito.mock(MatchAction.class);
    doReturn(ExpressionBuilder.from(new ExpressionClass("v")).build()).when(matchAction).getValue();
    doReturn(Map.of(
        ExpressionBuilder.from(new ExpressionClass("5")).build(), assignAction1,
        ExpressionBuilder.from(new ExpressionClass("6")).build(), assignAction2)).when(matchAction).getCasee();

    var matchActionCommand = new MatchActionCommand(stateMachine, matchAction, false);

    assertDoesNotThrow(() -> {
      var commands = matchActionCommand.execute(new ExecutionContext(stateMachine, null));

      for (var command : commands) {
        command.execute(new ExecutionContext(stateMachine, null));
      }

      assertEquals(localContext.get("v"), 6);
    });
  }
}
