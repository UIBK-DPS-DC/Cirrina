package at.ac.uibk.dps.cirrina.core.io.plantuml;

import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import java.util.ArrayList;
import java.util.List;

public class PlantUmlExporter {

  private final List<StateMachine> stateMachines = new ArrayList<>();

  public static PlantUmlExporter from(CollaborativeStateMachine collaborativeStateMachine) {
    var exporter = new PlantUmlExporter();
    collaborativeStateMachine.getStateMachines().forEach(exporter::withStateMachine);
    return exporter;
  }

  public static PlantUmlExporter from(StateMachine stateMachine) {
    return new PlantUmlExporter().withStateMachine(stateMachine);
  }

  public PlantUmlExporter withStateMachine(StateMachine stateMachine) {
    stateMachines.add(stateMachine);
    return this;
  }

  public String getPlantUml() {
    var builder = new StringBuilder();
    builder.append("@startuml\n");
    PlantUmlVisitor visitor = new PlantUmlVisitor();
    stateMachines.forEach(sm -> sm.accept(visitor));
    builder.append(visitor.getPlantUml());
    builder.append("@enduml");
    return builder.toString();
  }
}
