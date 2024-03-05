package at.ac.uibk.dps.cirrina.core.objects.transitions;

import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import java.util.List;

public class OnTransition extends Transition {

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
