package at.ac.uibk.dps.cirrina.core.objects.actions;

import java.util.Optional;

public abstract class Action {

  public final Optional<String> name;

  protected Action(Optional<String> name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name.orElse("Inline Action");
  }
}
