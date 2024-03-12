package at.ac.uibk.dps.cirrina.core.objects.actions;

import java.util.Optional;

/**
 * Invoke action, invokes a service type.
 */
public final class InvokeAction extends Action {

  public InvokeAction(Optional<String> name) {
    super(name);
  }
}
