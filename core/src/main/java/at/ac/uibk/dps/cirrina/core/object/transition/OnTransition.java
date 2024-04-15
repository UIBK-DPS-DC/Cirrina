package at.ac.uibk.dps.cirrina.core.object.transition;

import at.ac.uibk.dps.cirrina.core.io.plantuml.Exportable;
import at.ac.uibk.dps.cirrina.core.io.plantuml.PlantUmlVisitor;
import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.guard.Guard;
import java.util.List;
import java.util.Optional;

public final class OnTransition extends Transition implements Exportable {

  private final String eventName;

  OnTransition(String targetName, Optional<String> elsee, List<Guard> guards, List<Action> actions, String eventName) {
    super(targetName, elsee, guards, actions);

    this.eventName = eventName;
  }

  public String getEventName() {
    return eventName;
  }

  @Override
  public String toString() {
    return eventName;
  }

  @Override
  public void accept(PlantUmlVisitor visitor) {
    visitor.visit(this);
  }
}
