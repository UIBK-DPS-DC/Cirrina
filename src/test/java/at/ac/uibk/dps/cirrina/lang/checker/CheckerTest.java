package at.ac.uibk.dps.cirrina.lang.checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.lang.parser.Parser;
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
