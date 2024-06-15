package at.ac.uibk.dps.cirrina.execution.command;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.GAUGE_ACTION_DATA_LATENCY;

import at.ac.uibk.dps.cirrina.execution.aspect.logging.LogAction;
import at.ac.uibk.dps.cirrina.execution.aspect.logging.LogGeneral;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.TracesGeneral;
import at.ac.uibk.dps.cirrina.execution.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.execution.object.expression.Expression;
import at.ac.uibk.dps.cirrina.utils.Time;
import java.util.ArrayList;
import java.util.List;

public final class ActionCreateCommand extends ActionCommand {

  private final CreateAction createAction;

  ActionCreateCommand(ExecutionContext executionContext, CreateAction createAction) {
    super(executionContext);

    this.createAction = createAction;
  }

  @TracesGeneral
  @LogGeneral
  @LogAction
  @Override
  public List<ActionCommand> execute() throws UnsupportedOperationException {
    final var start = Time.timeInMillisecondsSinceStart();

    try {
      final var variable = createAction.getVariable();
      final var variableName = variable.name();

      final var commands = new ArrayList<ActionCommand>();

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

      return commands;
    } catch (Exception e) {
      throw new UnsupportedOperationException("Could not execute create action", e);
    }
  }
}
