package at.ac.uibk.dps.cirrina.core.objects.actions;

import at.ac.uibk.dps.cirrina.core.Event;
import java.util.Optional;

public class RaiseAction extends Action {

  public final Event event;

  public RaiseAction(Optional<String> name, Event event) {
    super(name);

    this.event = event;
  }
}
