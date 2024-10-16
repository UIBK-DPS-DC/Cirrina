package at.ac.uibk.dps.cirrina.execution.service;

import at.ac.uibk.dps.cirrina.execution.aspect.traces.Tracing;
import at.ac.uibk.dps.cirrina.tracing.TracingAttributes;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.*;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_PARENT_STATE_MACHINE_NAME;

public class RandomServiceImplementationSelector extends ServiceImplementationSelector {

  Tracing tracing = new Tracing();
  Tracer tracer = tracing.initializeTracer("Random Selector");

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
  public Optional<ServiceImplementation> select(String name, TracingAttributes tracingAttributes, boolean local, Span parentSpan) {
    Span span = tracing.initializeSpan("Random Service Selection", tracer, parentSpan,
            Map.of( ATTR_SERVICE_NAME, name,
                    ATTR_IS_LOCAL, String.valueOf(local),
                    ATTR_STATE_MACHINE_ID, tracingAttributes.getStateMachineId(),
                    ATTR_STATE_MACHINE_NAME, tracingAttributes.getStateMachineName(),
                    ATTR_PARENT_STATE_MACHINE_ID, tracingAttributes.getParentStateMachineId(),
                    ATTR_PARENT_STATE_MACHINE_NAME, tracingAttributes.getParentStateMachineName()));
    final var serviceImplementationsWithName = new ArrayList<ServiceImplementation>(local ?
        Multimaps.filterValues(serviceImplementations, ServiceImplementation::isLocal).get(name) :
        serviceImplementations.get(name));

    if (serviceImplementationsWithName.isEmpty()) {
      return Optional.empty();
    }

    ServiceImplementation randomImplementation = serviceImplementationsWithName.get(
        new Random().nextInt(serviceImplementationsWithName.size()));

    span.end();
    return Optional.of(randomImplementation);
  }
}
