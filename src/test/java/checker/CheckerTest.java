package checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import ac.at.uibk.dps.nexa.lang.checker.Checker;
import ac.at.uibk.dps.nexa.lang.parser.Parser;
import data.DefaultDescriptions;
import org.junit.jupiter.api.Test;

public class CheckerTest {

  @Test
  public void testCheckerPositive() {
    var json = DefaultDescriptions.complete;

    var parser = new Parser(new Parser.Options());
    assertDoesNotThrow(() -> {
      var csm = parser.parse(json);

      var checker = new Checker(new Checker.Options());
      checker.check(csm);
    });
  }
}
