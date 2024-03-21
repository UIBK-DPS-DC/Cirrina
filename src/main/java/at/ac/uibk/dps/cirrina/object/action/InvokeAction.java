package at.ac.uibk.dps.cirrina.object.action;

import at.ac.uibk.dps.cirrina.lang.classes.context.ContextVariableReference;
import at.ac.uibk.dps.cirrina.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.object.event.Event;
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

  public final Optional<ContextVariableReference> output;

  InvokeAction(Optional<String> name, String serviceType, boolean isLocal, List<ContextVariable> input,
               List<Event> done, Optional<ContextVariableReference> output) {
    super(name);

    this.serviceType = serviceType;
    this.isLocal = isLocal;
    this.input = input;
    this.done = done;
    this.output = output;
  }
}
