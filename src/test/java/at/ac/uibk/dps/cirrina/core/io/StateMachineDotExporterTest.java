package at.ac.uibk.dps.cirrina.core.io;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import at.ac.uibk.dps.cirrina.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.lang.checker.Checker;
import at.ac.uibk.dps.cirrina.lang.parser.Parser;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

public class StateMachineDotExporterTest {

  @Test
  public void testExportPositive() {
    var json = DefaultDescriptions.complete;

    var parser = new Parser(new Parser.Options());
    assertDoesNotThrow(() -> {
      var out = new StringWriter();
      StateMachineDotExporter.export(out,
          new Checker(new Checker.Options()).check(parser.parse(json))
              .getStateMachineByName("stateMachine1")
              .get());

      System.out.println(out);
    });
  }
}
