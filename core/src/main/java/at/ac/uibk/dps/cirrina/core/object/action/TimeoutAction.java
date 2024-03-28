package at.ac.uibk.dps.cirrina.core.object.action;

import java.util.Optional;

public final class TimeoutAction extends Action {

  TimeoutAction(Parameters parameters) {
    super(parameters.name());
  }

  public record Parameters(
      Optional<String> name
  ) {

  }
}
