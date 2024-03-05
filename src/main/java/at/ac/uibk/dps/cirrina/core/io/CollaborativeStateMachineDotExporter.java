package at.ac.uibk.dps.cirrina.core.io;

import at.ac.uibk.dps.cirrina.core.CoreException;
import at.ac.uibk.dps.cirrina.core.objects.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.objects.State;
import at.ac.uibk.dps.cirrina.core.objects.StateMachine;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.graphml.GraphMLExporter;

import java.io.Writer;

public class CollaborativeStateMachineDotExporter {

  public static void export(Writer out, CollaborativeStateMachine collaborativeStateMachineObject) throws CoreException {
    try {
      var exporter = new GraphMLExporter();

      exporter.setExportEdgeLabels(true);

      exporter.exportGraph(collaborativeStateMachineObject, out);
    } catch (IllegalArgumentException e) {
      throw new CoreException(
              String.format("Unexpected error while exporting a state machine object to DOT: %s", e.getMessage()));
    }
  }
}
