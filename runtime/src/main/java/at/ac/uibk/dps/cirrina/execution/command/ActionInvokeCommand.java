package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariable;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ActionInvokeCommand extends ActionCommand {

  private static final Logger logger = LogManager.getLogger();

  private final InvokeAction invokeAction;

  ActionInvokeCommand(ExecutionContext executionContext, InvokeAction invokeAction) {
    super(executionContext);

    this.invokeAction = invokeAction;
  }

  @Override
  public List<ActionCommand> execute() throws CirrinaException {
    final var commands = new ArrayList<ActionCommand>();

    final var extent = executionContext.scope().getExtent();

    final var serviceImplementationSelector = executionContext.serviceImplementationSelector();

    final var eventListener = executionContext.eventListener();

    final var serviceType = invokeAction.getServiceType();
    final var isLocal = invokeAction.isLocal();

    // Select a service implementation
    final var serviceImplementation = serviceImplementationSelector.select(serviceType, isLocal)
        .orElseThrow(() -> CirrinaException.from("Could not find a service implementation for the service type '%s'", serviceType));

    // Evaluate all input
    var input = new ArrayList<ContextVariable>();

    for (var variable : invokeAction.getInput()) {
      input.add(variable.evaluate(extent));
    }

    // Invoke (asynchronously)
    serviceImplementation.invoke(input)
        .exceptionally(e -> {
          logger.error("Service invocation failed: {}", e.getMessage());
          return null;
        }).thenAccept(output -> {
          // Create new events with output data as event data
          final var doneEvents = invokeAction.getDone().stream()
              .map(event -> {
                return event.withData(output);
              })
              .toList();

          // Raise all events (internally)
          doneEvents.forEach(eventListener::onReceiveEvent);
        });

    return commands;
  }
}
