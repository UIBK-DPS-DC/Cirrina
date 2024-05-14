package at.ac.uibk.dps.cirrina.execution.object.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import at.ac.uibk.dps.cirrina.csml.keyword.EventChannel;
import at.ac.uibk.dps.cirrina.execution.object.context.ContextVariable;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EventTest {

  @Test
  public void testToBytes() throws URISyntaxException {
    var contextVariable = Mockito.mock(ContextVariable.class);
    doReturn("varName").when(contextVariable).name();
    doReturn("some string").when(contextVariable).value();
    doReturn(false).when(contextVariable).isLazy();

    assertDoesNotThrow(() -> {
      var eventOut = new Event("name", EventChannel.EXTERNAL, List.of(contextVariable));
      var data = eventOut.toBytes();

      var eventIn = Event.fromBytes(data);

      assertEquals(eventIn.getId(), eventOut.getId());
      assertEquals(eventIn.getName(), "name");
      assertEquals(eventIn.getChannel(), EventChannel.EXTERNAL);
      assertEquals(eventIn.getData().getFirst().name(), "varName");
      assertEquals(eventIn.getData().getFirst().value(), "some string");
      assertEquals(eventIn.getData().getFirst().isLazy(), false);
    });
  }
}
