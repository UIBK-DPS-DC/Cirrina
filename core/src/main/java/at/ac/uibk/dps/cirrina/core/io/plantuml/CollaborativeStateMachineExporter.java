package at.ac.uibk.dps.cirrina.core.io.plantuml;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import java.io.IOException;
import java.io.Writer;

public class CollaborativeStateMachineExporter {

  private CollaborativeStateMachineExporter() {
  }

  public static void export(Writer out, CollaborativeStateMachine collaborativeStateMachine)
      throws CirrinaException {
    try {
      PlantUmlExporter plantUmlExporter = PlantUmlExporter.from(collaborativeStateMachine);
      out.write(plantUmlExporter.getPlantUml());
    } catch (IOException e) {
      throw new CirrinaException(
          String.format("Unexpected error while exporting a collaborative state machine object to puml: %s",
              e.getMessage()));
    }
  }

  public static void export(Writer out, StateMachine stateMachine)
      throws CirrinaException {
    try {
      PlantUmlExporter plantUmlExporter = PlantUmlExporter.from(stateMachine);
      out.write(plantUmlExporter.getPlantUml());
    } catch (IOException e) {
      throw new CirrinaException(
          String.format("Unexpected error while exporting a state machine object to puml: %s",
              e.getMessage()));
    }
  }
}
