package ac.at.uibk.dps.nexa.core.io;

import ac.at.uibk.dps.nexa.core.CoreException;
import ac.at.uibk.dps.nexa.core.objects.State;
import ac.at.uibk.dps.nexa.core.objects.StateMachine;
import java.io.Writer;
import org.jgrapht.nio.dot.DOTExporter;

public class StateMachineDotExporter {

  public static void export(Writer out, StateMachine stateMachineObject) throws CoreException {
    try {
      var exporter = new DOTExporter((vertex) -> {
        if (!(vertex instanceof State state)) {
          throw new IllegalArgumentException("expected a state object as the state machine graph vertex");
        }

        return state.name;
      });

      exporter.exportGraph(stateMachineObject, out);
    } catch (IllegalArgumentException e) {
      throw new CoreException(
          String.format("Unexpected error while exporting a state machine object to DOT: %s", e.getMessage()));
    }
  }
}
