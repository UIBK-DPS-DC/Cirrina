package at.ac.uibk.dps.cirrina.core.io;

import at.ac.uibk.dps.cirrina.core.CoreException;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import java.io.Writer;
import org.jgrapht.nio.graphml.GraphMLExporter;

public class StateMachineDotExporter {

  public static void export(Writer out, StateMachine stateMachineObject) throws CoreException {
    try {
      var exporter = new GraphMLExporter();

      exporter.setExportEdgeLabels(true);

      exporter.exportGraph(stateMachineObject, out);
    } catch (IllegalArgumentException e) {
      throw CoreException.from("Unexpected error while exporting a state machine object to DOT: %s", e.getMessage());
    }
  }
}
