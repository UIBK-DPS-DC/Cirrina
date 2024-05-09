package at.ac.uibk.dps.cirrina.runtime;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import io.opentelemetry.api.OpenTelemetry;

public final class DistributedRuntime extends Runtime {

  public DistributedRuntime(EventHandler eventHandler, Context persistentContext, OpenTelemetry openTelemetry) throws CirrinaException {
    super(eventHandler, persistentContext, openTelemetry);
  }
}
