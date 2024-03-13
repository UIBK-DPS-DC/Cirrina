package at.ac.uibk.dps.cirrina.core.object.transition;

import at.ac.uibk.dps.cirrina.core.object.action.Action;
import java.util.List;

public final class OnTransition extends Transition {

  public final String eventName;

  public OnTransition(String target, List<Action> actions, String eventName) {
    super(target, actions);

    this.eventName = eventName;
  }

  @Override
  public String toString() {
    return eventName;
  }
}
