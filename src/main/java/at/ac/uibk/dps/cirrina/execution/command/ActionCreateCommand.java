package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.GAUGE_ACTION_DATA_LATENCY;

import at.ac.uibk.dps.cirrina.execution.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.execution.object.expression.Expression;
import at.ac.uibk.dps.cirrina.utils.Time;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ActionCreateCommand extends ActionCommand {

  private static final Logger logger = LogManager.getLogger();

  private final CreateAction createAction;

  ActionCreateCommand(ExecutionContext executionContext, CreateAction createAction) {
    super(executionContext);

    this.createAction = createAction;
  }

  @Override
  public List<ActionCommand> execute(String stateMachineId, String stateMachineName, Span parentSpan) throws UnsupportedOperationException {
    logging.logAction(this.createAction.getName().isPresent() ? this.createAction.getName().get(): "null", stateMachineId, stateMachineName);
    Span span = tracing.initializeSpan("Create Action", tracer, parentSpan);
    tracing.addAttributes(Map.of(
        ATTR_STATE_MACHINE_ID, stateMachineId,
        ATTR_STATE_MACHINE_NAME, stateMachineName),span);
    try(Scope scope = span.makeCurrent()) {
      final var start = Time.timeInMillisecondsSinceStart();

    final var commands = new ArrayList<ActionCommand>();

    try {
      final var variable = createAction.getVariable();
      final var variableName = variable.name();

      final var extent = executionContext.scope().getExtent();

      final var isPersistent = createAction.isPersistent();

      // If the variable should be created persistently, we assume that the lowest priority context in the extent is the persistent context,
      // if the variable should not be created persistently, we assume that the highest priority context in the extent is the relevant local context
      final var targetContext = isPersistent ?
          extent.getLow() : // The lowest priority context in the extent is the persistent context
          extent.getHigh(); // The highest priority context in the extent is the local context in scope

      // Create the variable
      // Acquire the value, in case the variable is lazy, we have to find the value through evaluating the value expression
      Object value = null;
      if (variable.isLazy()) {
        final var expression = variable.value();

        assert expression instanceof Expression;
        value = ((Expression) expression).execute(extent);
      } else {
        value = variable.value();
      }

      // Attempt to create the variable
      final var size = targetContext.create(variableName, value);

      // Measure latency
      final var now = Time.timeInMillisecondsSinceStart();
      final var delta = now - start;

      final var gauges = executionContext.gauges();

      gauges.getGauge(GAUGE_ACTION_DATA_LATENCY).set(delta,
          gauges.attributesForData(
              "create",
              !isPersistent ? "local" : "persistent",
              size
          ));

    } catch (Exception e) {
      logging.logExeption(stateMachineId, e, stateMachineName);
      tracing.recordException(e, span);
      logger.error("Data creation failed: {}", e.getMessage());
    } finally {
      span.end();
    }

    return commands;
  }
}
}
