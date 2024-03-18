package at.ac.uibk.dps.cirrina.object.action;

import java.util.Optional;

/**
 * Base action, can represent and action.
 */
public abstract class Action {

  /**
   * The action name. An action name can be omitted, in which case it is an inline action and cannot be referenced.
   */
  public final Optional<String> name;

  /**
   * Initializes this action.
   *
   * @param name Name, can be optional in which case this action is inline.
   */
  Action(Optional<String> name) {
    this.name = name;
  }

  /**
   * To string.
   *
   * @return String representation.
   */
  @Override
  public String toString() {
    return name.orElse("Inline Action");
  }
}
