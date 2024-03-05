package core.io;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import ac.at.uibk.dps.nexa.core.io.StateMachineDotExporter;
import ac.at.uibk.dps.nexa.lang.checker.Checker;
import ac.at.uibk.dps.nexa.lang.parser.Parser;
import data.DefaultDescriptions;
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
          new Checker(new Checker.Options()).check(parser.parse(json)).getStateMachineByName("stateMachine1")
              .get());

      System.out.println(out);
    });
  }
}
