package ac.at.uibk.dps.nexa.core.objects.actions;

import ac.at.uibk.dps.nexa.core.Event;
import java.util.Optional;

public class RaiseAction extends Action {

  public final Event event;

  public RaiseAction(Optional<String> name, Event event) {
    super(name);

    this.event = event;
  }
}
