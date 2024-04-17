package at.ac.uibk.dps.cirrina.runtime.distributed;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.runtime.Runtime;
import at.ac.uibk.dps.cirrina.runtime.scheduler.Scheduler;

public final class DistributedRuntime extends Runtime {

  public DistributedRuntime(Scheduler scheduler, EventHandler eventHandler, Context persistentContext) throws RuntimeException {
    super(scheduler, eventHandler, persistentContext);
  }
}
