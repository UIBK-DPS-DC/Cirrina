package at.ac.uibk.dps.cirrina.execution.service;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.*;
import at.ac.uibk.dps.cirrina.execution.aspect.logging.Logging;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.Tracing;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class RandomServiceImplementationSelector extends ServiceImplementationSelector {
  final Tracing tracing = new Tracing();
  final Tracer tracer = tracing.initializeTracer("Random Selector");
  final Logging logging = new Logging();
  /**
   * Initializes this service implementation selector.
   *
   * @param serviceImplementations Known service implementations.
   */
  public RandomServiceImplementationSelector(Multimap<String, ServiceImplementation> serviceImplementations) {
    super(serviceImplementations);
  }

  /**
   * Selects, given the known service implementations, a random matching service implementation.
   *
   * @param name  Name of the requested service implementation.
   * @param local Whether the local implementation is required to be a local service implementation.
   * @return Selected service implementation.
   */
  @Override
  public Optional<ServiceImplementation> select(String name, boolean local, String stateMachineId, String stateMachineName, String parentStateMachineId, String parentStateMachineName, Span parentSpan) {
    Span span = tracing.initializeSpan(
        "Random Service Implementation Selection", tracer, parentSpan,
        Map.of(ATTR_SERVICE_NAME, name,
               ATTR_STATE_MACHINE_ID, stateMachineId,
               ATTR_STATE_MACHINE_NAME, stateMachineName,
               ATTR_PARENT_STATE_MACHINE_ID, parentStateMachineId,
               ATTR_PARENT_STATE_MACHINE_NAME, parentStateMachineName));

    final var serviceImplementationsWithName = new ArrayList<ServiceImplementation>(local ?
        Multimaps.filterValues(serviceImplementations, ServiceImplementation::isLocal).get(name) :
        serviceImplementations.get(name));

    if (serviceImplementationsWithName.isEmpty()) {
      return Optional.empty();
    }

    ServiceImplementation randomImplementation = serviceImplementationsWithName.get(
        new Random().nextInt(serviceImplementationsWithName.size()));

    return Optional.of(randomImplementation);
  }
}
