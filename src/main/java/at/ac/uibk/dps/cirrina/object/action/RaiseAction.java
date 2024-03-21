package at.ac.uibk.dps.cirrina.object.action;

import at.ac.uibk.dps.cirrina.object.event.Event;
import java.util.Optional;

public final class RaiseAction extends Action {

  public final Event event;

  RaiseAction(Parameters parameters) {
    super(parameters.name());

    this.event = parameters.event();
  }

  public record Parameters(
      Optional<String> name,
      Event event
  ) {

  }
}
