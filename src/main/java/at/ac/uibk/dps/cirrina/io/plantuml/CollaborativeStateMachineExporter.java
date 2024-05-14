package at.ac.uibk.dps.cirrina.io.plantuml;

import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClass;
import at.ac.uibk.dps.cirrina.classes.statemachine.StateMachineClass;
import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import java.io.IOException;
import java.io.Writer;

public class CollaborativeStateMachineExporter {

  private CollaborativeStateMachineExporter() {
  }

  public static void export(Writer out, CollaborativeStateMachineClass collaborativeStateMachineClass)
      throws CirrinaException {
    try {
      PlantUmlExporter plantUmlExporter = PlantUmlExporter.from(collaborativeStateMachineClass);
      out.write(plantUmlExporter.getPlantUml());
    } catch (IOException e) {
      throw CirrinaException.from("Unexpected error while exporting a collaborative state machine object to PlantUML: %s",
          e.getMessage());
    }
  }

  public static void export(Writer out, StateMachineClass stateMachineClass)
      throws CirrinaException {
    try {
      PlantUmlExporter plantUmlExporter = PlantUmlExporter.from(stateMachineClass);
      out.write(plantUmlExporter.getPlantUml());
    } catch (IOException e) {
      throw CirrinaException.from("Unexpected error while exporting a state machine object to PlantUML: %s",
          e.getMessage());
    }
  }
}
