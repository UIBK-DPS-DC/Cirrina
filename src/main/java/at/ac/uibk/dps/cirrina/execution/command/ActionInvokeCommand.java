package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.COUNTER_INVOCATIONS;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.GAUGE_ACTION_INVOKE_LATENCY;

import at.ac.uibk.dps.cirrina.execution.aspect.logging.LogAction;
import at.ac.uibk.dps.cirrina.execution.aspect.logging.LogGeneral;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.TracesGeneral;
import at.ac.uibk.dps.cirrina.execution.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.execution.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.utils.Time;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Action invoke command, performs a service type invocation.
 * <p>
 * The execution of this command will use the service implementation selector to select the best matching service implementation and perform
 * the invocation.
 * <p>
 * An invocation is always asynchronous and subsequent done events are generated and handled by the containing state machine instance.
 */
public final class ActionInvokeCommand extends ActionCommand {

  private static final Logger logger = LogManager.getLogger();

  private final InvokeAction invokeAction;

  /**
   * Initializes this action invoke commands.
   *
   * @param executionContext Execution context.
   * @param invokeAction     Invoke action.
   */
  ActionInvokeCommand(ExecutionContext executionContext, InvokeAction invokeAction) {
    super(executionContext);

    this.invokeAction = invokeAction;
  }

  @TracesGeneral
  @LogGeneral
  @LogAction
  @Override
  public List<ActionCommand> execute() throws UnsupportedOperationException {
    // Increment events received counter
    final var counters = executionContext.counters();

    counters.getCounter(COUNTER_INVOCATIONS).add(1,
        counters.attributesForInvocation());

    final var start = Time.timeInMillisecondsSinceStart();

    try {
      final var serviceType = invokeAction.getServiceType();
      final var isLocal = invokeAction.isLocal();

      final var serviceImplementationSelector = executionContext.serviceImplementationSelector();

      // Select a service implementation
      final var serviceImplementation = serviceImplementationSelector.select(serviceType, isLocal)
          .orElseThrow(
              () -> new IllegalArgumentException(
                  "Could not find a service implementation for the service type '%s'".formatted(serviceType)));

      final var commands = new ArrayList<ActionCommand>();

      final var extent = executionContext.scope().getExtent();
      final var eventListener = executionContext.eventListener();

      // Evaluate all input
      var input = new ArrayList<ContextVariable>();

      for (var variable : invokeAction.getInput()) {
        input.add(variable.evaluate(extent));
      }

      // Invoke (asynchronously)
      serviceImplementation.invoke(input, executionContext.scope().getId())
          .exceptionally(e -> {
            logger.error("Service invocation failed: {}", serviceImplementation.getInformationString());
            return null;
          }).thenAccept(output -> {
            // Assign service output to the provided output context variables
            for (final var outputReference : invokeAction.getOutput()) {

              // Find output reference in the service output
              final var outputVariable = output.stream()
                  .filter(variable -> variable.name().equals(outputReference.reference))
                  .findFirst();

              if (outputVariable.isEmpty()) {
                logger.warn(
                    "Service invocation output does not contain the expected output variable '{}'.",
                    outputReference.reference
                );
                continue;
              }

              try {
                extent.trySet(outputReference.reference, outputVariable.get().value());
              } catch (Exception e) {
                logger.error(
                    "Failed to assign service output to output variable '{}': {}",
                    outputReference.reference, e.getMessage()
                );
              }
            }

            // Create new events with output data as event data
            final var doneEvents = invokeAction.getDone().stream()
                .map(event -> event.withData(output))
                .toList();

            // Raise all events (internally)
            doneEvents.forEach(eventListener::onReceiveEvent);

            // Measure latency
            final var now = Time.timeInMillisecondsSinceStart();
            final var delta = now - start;

            final var gauges = executionContext.gauges();

            gauges.getGauge(GAUGE_ACTION_INVOKE_LATENCY).set(delta,
                gauges.attributesForInvocation(
                    serviceImplementation.isLocal() ? "local" : "remote"
                ));
          });

      return commands;
    } catch (Exception e) {
      throw new UnsupportedOperationException("Could not execute invoke action", e);
    }
  }
}
