package ac.at.uibk.dps.nexa.core.objects.actions;

import java.util.Optional;

public abstract class Action {

  public final Optional<String> name;

  public Action(Optional<String> name) {
    this.name = name;
  }
}
