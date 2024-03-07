package at.ac.uibk.dps.cirrina.core.io;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.lang.checker.Checker;
import at.ac.uibk.dps.cirrina.lang.parser.Parser;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

public class CollaborativeStateMachineDotExporterTest {

  @Test
  public void testExportPositive() {
    var json = DefaultDescriptions.complete;

    var parser = new Parser(new Parser.Options());
    assertDoesNotThrow(() -> {
      var out = new StringWriter();
      CollaborativeStateMachineDotExporter.export(out,
          new Checker(new Checker.Options()).check(parser.parse(json)));

      System.out.println(out);
    });
  }
}
