package at.ac.uibk.dps.cirrina.core.object.action;

import at.ac.uibk.dps.cirrina.core.object.context.Context.ContextVariable;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import java.util.List;
import java.util.Optional;

/**
 * Invoke action, invokes a service type.
 */
public final class InvokeAction extends Action {

  public final String serviceType;

  public final boolean isLocal;

  public final List<ContextVariable> input;

  public final List<Event> done;

  InvokeAction(Optional<String> name, String serviceType, boolean isLocal, List<ContextVariable> input, List<Event> done) {
    super(name);

    this.serviceType = serviceType;
    this.isLocal = isLocal;
    this.input = input;
    this.done = done;
  }
}
