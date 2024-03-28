package at.ac.uibk.dps.cirrina.core.lang.checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import at.ac.uibk.dps.cirrina.core.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.core.lang.parser.Parser;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import org.junit.jupiter.api.Test;

public class CheckerTest {

  @Test
  public void testCheckerPositive() {
    var json = DefaultDescriptions.complete;

    var parser = new Parser(new Parser.Options());
    assertDoesNotThrow(() -> {
      var csm = parser.parse(json);

      CollaborativeStateMachineBuilder.from(csm).build();
    });
  }
}
