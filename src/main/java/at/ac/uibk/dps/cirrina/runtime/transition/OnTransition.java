package at.ac.uibk.dps.cirrina.runtime.transition;

import at.ac.uibk.dps.cirrina.runtime.action.Action;
import java.util.List;
import java.util.Optional;

public final class OnTransition extends Transition {

  public final String eventName;

  OnTransition(String target, Optional<String> elsee, List<Action> actions, String eventName) {
    super(target, elsee, actions);

    this.eventName = eventName;
  }

  @Override
  public String toString() {
    return eventName;
  }
}
