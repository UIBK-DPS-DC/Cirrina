package parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ac.at.uibk.dps.nexa.lang.parser.Parser;
import ac.at.uibk.dps.nexa.lang.parser.Parser.Options;
import ac.at.uibk.dps.nexa.lang.parser.ParserException;
import data.DefaultDescriptions;
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
}
