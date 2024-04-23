package at.ac.uibk.dps.cirrina.runtime.command.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.lang.classes.ExpressionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.context.ContextVariableClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.event.EventChannel;
import at.ac.uibk.dps.cirrina.core.lang.classes.event.EventClass;
import at.ac.uibk.dps.cirrina.core.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.context.InMemoryContext;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.core.object.event.EventBuilder;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.runtime.command.Command.ExecutionContext;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance.InstanceId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RaiseActionCommandTest {

  @Test
  public void testRaiseActionCommand() throws InterruptedException {
    var latch = new CountDownLatch(5);

    var eventHandler = new EventHandler() {

      public final List<Event> events = new ArrayList<>();

      @Override
      public void close() throws Exception {

      }

      @Override
      public void sendEvent(Event event, String source) throws CirrinaException {
        events.add(event);
        latch.countDown();
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

    var persistentContext = new InMemoryContext();
    var localContext = new InMemoryContext();

    assertDoesNotThrow(() -> {
      localContext.create("v", 5);
    });

    var stateMachine = Mockito.mock(StateMachineInstance.class);
    doReturn(new Extent(persistentContext, localContext)).when(stateMachine).getExtent();
    doReturn(new InstanceId()).when(stateMachine).getInstanceId();

    var expressionClass = new ExpressionClass("5");

    var contextVariableClass = new ContextVariableClass();
    contextVariableClass.name = "varName";
    contextVariableClass.value = expressionClass;

    var eventClass = new EventClass();
    eventClass.channel = EventChannel.EXTERNAL;
    eventClass.name = "e1";
    eventClass.data = List.of(contextVariableClass);

    var e1 = EventBuilder.from(eventClass).build();

    var raiseAction = Mockito.mock(RaiseAction.class);
    doReturn(e1).when(raiseAction).getEvent();

    var raiseActionCommand = new RaiseActionCommand(stateMachine, raiseAction, false);

    assertDoesNotThrow(() -> {
      for (int i = 0; i < 5; ++i) {
        raiseActionCommand.execute(new ExecutionContext(stateMachine, eventHandler));
      }
    });

    latch.await();

    assertEquals(eventHandler.events.size(), 5);

    for (var e : eventHandler.events) {
      assertEquals(e.getName(), "e1");
      assertEquals(e.getChannel(), EventChannel.EXTERNAL);
      assertEquals(e.getData().size(), 1);

      var ed = e.getData().getFirst();

      assertEquals(ed.name(), "varName");
      assertEquals(ed.value(), 5);
      assertFalse(ed.isLazy());
    }
  }
}
