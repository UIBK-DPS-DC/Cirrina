package at.ac.uibk.dps.cirrina.core.io;

import at.ac.uibk.dps.cirrina.core.CoreException;
import at.ac.uibk.dps.cirrina.core.objects.CollaborativeStateMachine;
import java.io.Writer;
import org.jgrapht.nio.graphml.GraphMLExporter;

public class CollaborativeStateMachineDotExporter {

  public static void export(Writer out, CollaborativeStateMachine collaborativeStateMachineObject)
      throws CoreException {
    try {
      var exporter = new GraphMLExporter();

      exporter.setExportEdgeLabels(true);

      exporter.exportGraph(collaborativeStateMachineObject, out);
    } catch (IllegalArgumentException e) {
      throw new CoreException(
          String.format("Unexpected error while exporting a state machine object to DOT: %s",
              e.getMessage()));
    }
  }
}
