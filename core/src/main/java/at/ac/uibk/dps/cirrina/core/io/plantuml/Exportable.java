package at.ac.uibk.dps.cirrina.core.io.plantuml;

public interface Exportable {

  void accept(PlantUmlVisitor visitor);
}
