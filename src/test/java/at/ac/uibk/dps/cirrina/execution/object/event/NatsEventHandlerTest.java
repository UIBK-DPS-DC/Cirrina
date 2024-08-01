package at.ac.uibk.dps.cirrina.execution.object.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import at.ac.uibk.dps.cirrina.csml.description.ExpressionDescription;
import at.ac.uibk.dps.cirrina.csml.description.context.ContextVariableDescription;
import at.ac.uibk.dps.cirrina.csml.description.event.EventDescription;
import at.ac.uibk.dps.cirrina.csml.keyword.EventChannel;
import at.ac.uibk.dps.cirrina.execution.object.context.Extent;
import at.ac.uibk.dps.cirrina.execution.object.context.InMemoryContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class NatsEventHandlerTest {

  @Test
  public void testNatsEventHandlerSendReceiveGlobal() throws Exception {
    String natsServerURL = System.getenv("NATS_SERVER_URL");

    Assumptions.assumeFalse(natsServerURL == null, "Skipping NATS event handler test");

    var latch = new CountDownLatch(5);

    var eventListener = new EventListener() {

      public List<Event> events = new ArrayList<>();

      @Override
      public boolean onReceiveEvent(Event event) {
        events.add(event);

        latch.countDown();

        return true;
      }
    };

    var localContext = new InMemoryContext(true);

    var natsEventHandler = new NatsEventHandler(natsServerURL);

    var expressionClass = new ExpressionDescription("5");

    var contextVariableClass = new ContextVariableDescription();
    contextVariableClass.name = "varName";
    contextVariableClass.value = expressionClass;

    var eventClass = new EventDescription();
    eventClass.channel = EventChannel.GLOBAL;
    eventClass.name = "e1";
    eventClass.data = List.of(contextVariableClass);

    var e1 = EventBuilder.from(eventClass).build();

    natsEventHandler.addListener(eventListener);
    natsEventHandler.subscribe("e1");

    assertDoesNotThrow(() -> {
      for (int i = 0; i < 5; ++i) {
        natsEventHandler.sendEvent(Event.ensureHasEvaluatedData(e1, new Extent(localContext)), "source");
      }
    });

    latch.await();

    assertEquals(5, eventListener.events.size());

    for (var e : eventListener.events) {
      assertEquals("e1", e.getName());
      assertEquals(EventChannel.GLOBAL, e.getChannel());
      assertEquals(1, e.getData().size());

      var ed = e.getData().getFirst();

      assertEquals("varName", ed.name());
      assertEquals(5, ed.value());
      assertFalse(ed.isLazy());
    }

    eventListener.events.clear();

    natsEventHandler.unsubscribe("e1");

    assertDoesNotThrow(() -> {
      for (int i = 0; i < 5; ++i) {
        natsEventHandler.sendEvent(Event.ensureHasEvaluatedData(e1, new Extent(localContext)), "source");
      }
    });

    assertEquals(0, eventListener.events.size());

    natsEventHandler.unsubscribe("e1");

    natsEventHandler.close();
  }

  @Test
  public void testNatsEventHandlerSendReceiveExternal() throws Exception {
    String natsServerURL = System.getenv("NATS_SERVER_URL");

    Assumptions.assumeFalse(natsServerURL == null, "Skipping NATS event handler test");

    var latch = new CountDownLatch(5);

    var eventListener = new EventListener() {

      public List<Event> events = new ArrayList<>();

      @Override
      public boolean onReceiveEvent(Event event) {
        events.add(event);

        latch.countDown();

        return true;
      }
    };

    var localContext = new InMemoryContext(true);

    var natsEventHandler = new NatsEventHandler(natsServerURL);

    var expressionClass = new ExpressionDescription("5");

    var contextVariableClass = new ContextVariableDescription();
    contextVariableClass.name = "varName";
    contextVariableClass.value = expressionClass;

    var eventClass = new EventDescription();
    eventClass.channel = EventChannel.EXTERNAL;
    eventClass.name = "e1";
    eventClass.data = List.of(contextVariableClass);

    var e1 = EventBuilder.from(eventClass).build();

    natsEventHandler.addListener(eventListener);
    natsEventHandler.subscribe("source", "e1");

    assertDoesNotThrow(() -> {
      for (int i = 0; i < 5; ++i) {
        natsEventHandler.sendEvent(Event.ensureHasEvaluatedData(e1, new Extent(localContext)), "source");
      }
    });

    latch.await();

    assertEquals(5, eventListener.events.size());

    for (var e : eventListener.events) {
      assertEquals("e1", e.getName());
      assertEquals(EventChannel.EXTERNAL, e.getChannel());
      assertEquals(1, e.getData().size());

      var ed = e.getData().getFirst();

      assertEquals("varName", ed.name());
      assertEquals(5, ed.value());
      assertFalse(ed.isLazy());
    }

    eventListener.events.clear();

    natsEventHandler.unsubscribe("source", "e1");

    assertDoesNotThrow(() -> {
      for (int i = 0; i < 5; ++i) {
        natsEventHandler.sendEvent(Event.ensureHasEvaluatedData(e1, new Extent(localContext)), "source");
      }
    });

    assertEquals(0, eventListener.events.size());

    natsEventHandler.unsubscribe("source", "e1");

    natsEventHandler.close();
  }
}
