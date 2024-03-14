package at.ac.uibk.dps.cirrina.lang.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.exception.ParserException;
import at.ac.uibk.dps.cirrina.lang.parser.Parser.Options;
import org.junit.jupiter.api.Test;


public class JsonParserTest {

  @Test
  public void testDescriptionPositive() {
    var json = DefaultDescriptions.complete;

    var parser = new Parser(new Options());
    assertDoesNotThrow(() -> {
      var csm = parser.parse(json);
    });
  }

  @Test
  public void testDescriptionNegative() {
    var json = DefaultDescriptions.empty;

    var parser = new Parser(new Options());
    assertThrows(ParserException.class, () -> {
      var csm = parser.parse(json);
      System.out.println(csm);
    });
  }

  @Test
  public void testInheritance() {
    var json = DefaultDescriptions.completeInheritance;

    var parser = new Parser(new Options());
    assertDoesNotThrow(() -> {
      var csm = parser.parse(json);
    });
  }
}
