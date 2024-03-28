package at.ac.uibk.dps.cirrina.core.object.action;

import at.ac.uibk.dps.cirrina.core.object.event.Event;
import java.util.Optional;

public final class RaiseAction extends Action {

  private final Event event;

  RaiseAction(Parameters parameters) {
    super(parameters.name());

    this.event = parameters.event();
  }

  public Event getEvent() {
    return event;
  }

  public record Parameters(
      Optional<String> name,
      Event event
  ) {

  }
}
