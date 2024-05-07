package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;

public final class DistributedRuntime extends Runtime {

  public DistributedRuntime(EventHandler eventHandler, Context persistentContext) throws CirrinaException {
    super(eventHandler, persistentContext);
  }
}
