package at.ac.uibk.dps.cirrina.core.object.action;

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
