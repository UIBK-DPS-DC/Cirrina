package at.ac.uibk.dps.cirrina.core.runtime.action;

import at.ac.uibk.dps.cirrina.core.runtime.event.Event;
import java.util.Optional;

public final class RaiseAction extends Action {

  public final Event event;

  RaiseAction(Optional<String> name, Event event) {
    super(name);

    this.event = event;
  }
}
