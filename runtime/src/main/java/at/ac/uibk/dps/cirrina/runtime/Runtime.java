package at.ac.uibk.dps.cirrina.runtime;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_PARENT_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.SPAN_NEW_INSTANCE;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.core.utils.BuildVersion;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstanceId;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract runtime, provides the generic runtime behavior of running state machine instances.
 * <p>
 * The abstract runtime is either specialized as a shared runtime, providing the non-distributed execution of a collaborative state machine,
 * or as a distributed runtime is a specialization of the abstract runtime that provides the distributed execution of a set of state machine
 * instances.
 *
 * @see SharedRuntime
 * @see DistributedRuntime
 */
public abstract class Runtime implements EventListener {

  private static final Logger logger = LogManager.getLogger();

  private final ExecutorService executorService = Executors.newCachedThreadPool();

  private final ReentrantLock stateMachineInstancesLock = new ReentrantLock();

  private final List<StateMachineInstance> stateMachineInstances = new ArrayList<>();

  private final EventHandler eventHandler;

  private final Context persistentContext;

  private final OpenTelemetry openTelemetry;

  private final Tracer tracer;

  public Runtime(EventHandler eventHandler, Context persistentContext, OpenTelemetry openTelemetry) throws CirrinaException {
    this.eventHandler = eventHandler;
    this.persistentContext = persistentContext;
    this.openTelemetry = openTelemetry;

    // Create an OpenTelemetry tracer
    this.tracer = this.openTelemetry.getTracer("runtime", BuildVersion.getBuildVersion());
  }

  protected StateMachineInstanceId newInstance(
      StateMachine stateMachine,
      ServiceImplementationSelector serviceImplementationSelector,
      @Nullable StateMachineInstanceId parentId)
      throws CirrinaException {
    final var stateMachineName = stateMachine.getName();
    final var parentIdAsString = parentId == null ? "" : parentId.toString();

    logger.info("Creating new instance of '{}' - parent is '{}'...", stateMachineName, parentIdAsString);

    // Create span
    final var span = tracer.spanBuilder(SPAN_NEW_INSTANCE).startSpan();

    // Span attributes
    span.setAttribute(ATTR_STATE_MACHINE_NAME, stateMachineName);
    span.setAttribute(ATTR_STATE_MACHINE_PARENT_ID, parentIdAsString);

    try (final var scope = span.makeCurrent()) {
      stateMachineInstancesLock.lock();

      // Find the parent state machine instance
      final var parentInstance = parentId == null ? null : findInstance(parentId).orElse(null);

      if (parentId != null && parentInstance == null) {
        throw CirrinaException.from("The parent state machine instance with id '%s' could not be found", parentId.toString());
      }

      // Create the state machine instance
      final var stateMachineInstance = new StateMachineInstance(this, stateMachine, serviceImplementationSelector, parentInstance,
          openTelemetry);

      // Add event listener to the event handler
      eventHandler.addListener(stateMachineInstance);

      // Add to the collection of state machine instances
      stateMachineInstances.add(stateMachineInstance);

      // Execute
      executorService.execute(stateMachineInstance);

      final var stateMachineInstanceId = stateMachineInstance.getStateMachineInstanceId();

      logger.info("Created an instance of '{}' with ID '{}'", stateMachineName, stateMachineInstanceId.toString());

      return stateMachineInstanceId;
    } finally {
      stateMachineInstancesLock.unlock();

      span.end();
    }
  }

  public boolean shutdown(long timeoutInMs) {
    logger.info("Shutting down runtime");

    try {
      stateMachineInstancesLock.lock();

      executorService.shutdown();

      return executorService.awaitTermination(timeoutInMs, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      stateMachineInstancesLock.unlock();
    }

    return false;
  }

  /**
   * Find a state machine instance based on its instance id.
   *
   * @param stateMachineInstanceId State machine instance id.
   * @return The state machine instance or an empty optional if no state machine instance was found for the given instance id.
   */
  public Optional<StateMachineInstance> findInstance(StateMachineInstanceId stateMachineInstanceId) {
    try {
      stateMachineInstancesLock.lock();

      return stateMachineInstances.stream()
          .filter(stateMachineInstance -> stateMachineInstance.getStateMachineInstanceId().equals(stateMachineInstanceId))
          .findFirst();
    } finally {
      stateMachineInstancesLock.unlock();
    }
  }

  public Extent getExtent() {
    return new Extent(persistentContext);
  }

  public EventHandler getEventHandler() {
    return eventHandler;
  }
}
