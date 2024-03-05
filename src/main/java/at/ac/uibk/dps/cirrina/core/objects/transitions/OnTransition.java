package at.ac.uibk.dps.cirrina.core.objects.transitions;

import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import java.util.List;

public class OnTransition extends Transition {

  public final String event;

  public OnTransition(String target, List<Action> actions, String event) {
    super(target, actions);

    this.event = event;
  }
}
