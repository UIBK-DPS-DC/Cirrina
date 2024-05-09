package at.ac.uibk.dps.cirrina.core.lang.parser;

import at.ac.uibk.dps.cirrina.core.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class JsonParserTest {

  @Test
  public void testDescriptionPositive() {
    var json = DefaultDescriptions.complete;

    var parser = new CollaborativeStateMachineParser();
    assertDoesNotThrow(() -> {
      var csm = parser.parse(json);
    });
  }

  @Test
  public void testDescriptionNegative() {
    var json = DefaultDescriptions.empty;

    var parser = new CollaborativeStateMachineParser();
    assertThrows(CirrinaException.class, () -> {
      var csm = parser.parse(json);
      System.out.println(csm);
    });
  }

  @Test
  public void testInheritance() {
    var json = DefaultDescriptions.completeInheritance;

    var parser = new CollaborativeStateMachineParser();
    assertDoesNotThrow(() -> {
      var csm = parser.parse(json);
    });
  }
}
