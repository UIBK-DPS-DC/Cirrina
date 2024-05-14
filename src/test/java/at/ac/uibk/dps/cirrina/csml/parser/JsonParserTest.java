package at.ac.uibk.dps.cirrina.csml.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription;
import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.io.description.DescriptionParser;
import org.junit.jupiter.api.Test;


public class JsonParserTest {

  @Test
  public void testDescriptionPositive() {
    var json = DefaultDescriptions.complete;

    var parser = new DescriptionParser<CollaborativeStateMachineDescription>(CollaborativeStateMachineDescription.class);
    assertDoesNotThrow(() -> {
      var csm = parser.parse(json);
    });
  }

  @Test
  public void testDescriptionNegative() {
    var json = DefaultDescriptions.empty;

    var parser = new DescriptionParser<CollaborativeStateMachineDescription>(CollaborativeStateMachineDescription.class);
    assertThrows(CirrinaException.class, () -> {
      var csm = parser.parse(json);
      System.out.println(csm);
    });
  }

  @Test
  public void testInheritance() {
    var json = DefaultDescriptions.completeInheritance;

    var parser = new DescriptionParser<CollaborativeStateMachineDescription>(CollaborativeStateMachineDescription.class);
    assertDoesNotThrow(() -> {
      var csm = parser.parse(json);
    });
  }
}
