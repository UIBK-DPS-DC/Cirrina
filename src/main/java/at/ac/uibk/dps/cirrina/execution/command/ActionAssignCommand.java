package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_ID;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.ATTR_STATE_MACHINE_NAME;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.GAUGE_ACTION_DATA_LATENCY;

import at.ac.uibk.dps.cirrina.execution.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.execution.object.expression.Expression;
import at.ac.uibk.dps.cirrina.utils.Time;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ActionAssignCommand extends ActionCommand {

  private static final Logger logger = LogManager.getLogger();

  private final AssignAction assignAction;

  ActionAssignCommand(ExecutionContext executionContext, AssignAction assignAction) {
    super(executionContext);

    this.assignAction = assignAction;
  }

  @Override
  public List<ActionCommand> execute(String stateMachineId, String stateMachineName, Span parentSpan) throws UnsupportedOperationException {
    logging.logAction(this.assignAction.getName().isPresent() ? this.assignAction.getName().get(): "null", stateMachineId, stateMachineName);
    Span span = tracing.initializeSpan("Assign Action", tracer, parentSpan);
    tracing.addAttributes(Map.of(
        ATTR_STATE_MACHINE_ID, stateMachineId,
        ATTR_STATE_MACHINE_NAME, stateMachineName),span);
    try(Scope scope = span.makeCurrent()) {
      final var start = Time.timeInMillisecondsSinceStart();

      final var commands = new ArrayList<ActionCommand>();

      try {
        final var variable = assignAction.getVariable();
        final var variableName = variable.name();

        final var extent = executionContext.scope().getExtent();

        // Acquire the value, in case the variable is lazy, we have to find the value through evaluating the value expression
        Object value = null;
        if (variable.isLazy()) {
          final var expression = variable.value();

          assert expression instanceof Expression;
          value = ((Expression) expression).execute(extent);
        } else {
          value = variable.value();

        }

        // Attempt to set the variable
        final var result = extent.trySet(variableName, value);


        // Measure latency
        final var now = Time.timeInMillisecondsSinceStart();
        final var delta = now - start;

        final var gauges = executionContext.gauges();

        gauges.getGauge(GAUGE_ACTION_DATA_LATENCY).set(delta,
            gauges.attributesForData(
                "assign",
                result.context().isLocal() ? "local" : "persistent",
                result.size()
            ));

      } catch (IOException e) {
        logging.logExeption(stateMachineId, e, stateMachineName);
        tracing.recordException(e, span);
        logger.error("Data assignment failed: {}", e.getMessage());
    }finally {
      span.end();
    }

    return commands;
  }
}
}
