package at.ac.uibk.dps.cirrina.object.action;

import java.util.Optional;

public final class TimeoutResetAction extends Action {

  TimeoutResetAction(Parameters parameters) {
    super(parameters.name());
  }

  public record Parameters(
      Optional<String> name
  ) {

  }
}
