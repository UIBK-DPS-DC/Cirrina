package ac.at.uibk.dps.nexa.core.objects.transitions;

import ac.at.uibk.dps.nexa.core.objects.actions.Action;
import java.util.List;

public class OnTransition extends Transition {

  public final String event;

  public OnTransition(String target, List<Action> actions, String event) {
    super(target, actions);

    this.event = event;
  }
}
