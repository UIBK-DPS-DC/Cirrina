package at.ac.uibk.dps.cirrina.core.io.plantuml;

import at.ac.uibk.dps.cirrina.core.data.DefaultDescriptions;
import at.ac.uibk.dps.cirrina.core.lang.parser.CollaborativeStateMachineParser;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CollaborativeStateMachineExporterTest {

  private static CollaborativeStateMachine completeNestedCsm;

  @BeforeAll
  static void setUp() {
    var json = DefaultDescriptions.completeNested;

    var parser = new CollaborativeStateMachineParser();
    var csm = assertDoesNotThrow(() -> parser.parse(json));
    completeNestedCsm = assertDoesNotThrow(() -> CollaborativeStateMachineBuilder.from(csm).build());
  }

  @Test
  void exportCollaborativeStateMachine() {
    var out = new StringWriter();

    assertDoesNotThrow(() -> {
      CollaborativeStateMachineExporter.export(out, completeNestedCsm);

      var file = new File("test_complete_nested_csm.puml");
      assertDoesNotThrow(() -> {
        var writer = new FileWriter(file);
        writer.write(out.toString());
        writer.close();
      });
    });
  }

  @Test
  void exportStateMachine() {
    var out = new StringWriter();

    assertDoesNotThrow(() -> {
      CollaborativeStateMachineExporter.export(out, completeNestedCsm.getStateMachines().getFirst());

      var file = new File("test_complete_nested_sm.puml");
      assertDoesNotThrow(() -> {
        var writer = new FileWriter(file);
        writer.write(out.toString());
        writer.close();
      });
    });
  }
}
