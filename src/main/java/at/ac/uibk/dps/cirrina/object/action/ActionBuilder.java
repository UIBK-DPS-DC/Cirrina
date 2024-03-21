package at.ac.uibk.dps.cirrina.object.action;

import at.ac.uibk.dps.cirrina.lang.classes.action.ActionClass;
import at.ac.uibk.dps.cirrina.lang.classes.action.AssignActionClass;
import at.ac.uibk.dps.cirrina.lang.classes.action.CreateActionClass;
import at.ac.uibk.dps.cirrina.lang.classes.action.InvokeActionClass;
import at.ac.uibk.dps.cirrina.lang.classes.action.MatchActionClass;
import at.ac.uibk.dps.cirrina.lang.classes.action.RaiseActionClass;
import at.ac.uibk.dps.cirrina.lang.classes.action.TimeoutActionClass;
import at.ac.uibk.dps.cirrina.lang.classes.action.TimeoutResetActionClass;
import at.ac.uibk.dps.cirrina.lang.classes.context.ContextVariableClass;
import at.ac.uibk.dps.cirrina.lang.classes.event.EventClass;
import at.ac.uibk.dps.cirrina.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.object.context.ContextVariableBuilder;
import at.ac.uibk.dps.cirrina.object.event.Event;
import at.ac.uibk.dps.cirrina.object.event.EventBuilder;
import at.ac.uibk.dps.cirrina.object.expression.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.object.helper.ActionResolver;
import java.util.HashMap;
import java.util.List;

/**
 * Action builder, used to build action objects.
 */
public final class ActionBuilder {

  /**
   * The action class to build from.
   */
  private final ActionClass actionClass;

  private final ActionResolver actionResolver;

  /**
   * Initializes an action builder.
   *
   * @param actionClass Action class.
   */
  private ActionBuilder(ActionClass actionClass, ActionResolver actionResolver) {
    this.actionClass = actionClass;
    this.actionResolver = actionResolver;
  }

  /**
   * Creates an action builder.
   *
   * @param actionClass Action class.
   * @return Action builder.
   */
  public static ActionBuilder from(ActionClass actionClass, ActionResolver actionResolver) {
    return new ActionBuilder(actionClass, actionResolver);
  }

  private static List<ContextVariable> buildVariableList(List<ContextVariableClass> contextVariableClasses) {
    return contextVariableClasses.stream()
        .map(c -> ContextVariableBuilder.from(c).build())
        .toList();
  }

  private static List<Event> buildEvents(List<EventClass> eventClasses) {
    return eventClasses.stream()
        .map(e -> EventBuilder.from(e).build())
        .toList();
  }

  /**
   * Builds the action.
   *
   * @return The built action.
   * @throws IllegalArgumentException In case the action could not be built.
   * @throws IllegalStateException    In case of an unexpected state.
   */
  public Action build() throws IllegalArgumentException, IllegalStateException {
    switch (actionClass) {
      case AssignActionClass assign -> {
        var contextVariable = ContextVariableBuilder.from(assign.variable).build();

        var parameters = new AssignAction.Parameters(
            assign.name,
            contextVariable
        );

        return new AssignAction(parameters);
      }
      case CreateActionClass create -> {
        var contextVariable = ContextVariableBuilder.from(create.variable).build();

        var parameters = new CreateAction.Parameters(
            create.name,
            contextVariable,
            create.isPersistent
        );

        return new CreateAction(parameters);
      }
      case InvokeActionClass invoke -> {
        var parameters = new InvokeAction.Parameters(
            invoke.name,
            invoke.serviceType,
            invoke.isLocal,
            buildVariableList(invoke.input),
            buildEvents(invoke.done),
            invoke.output
        );

        return new InvokeAction(parameters);
      }
      case MatchActionClass match -> {
        var parameters = new MatchAction.Parameters(
            match.name,
            ExpressionBuilder.from(match.value).build(),
            new HashMap<>() // TODO: Initialize
        );

        return new MatchAction(parameters);
      }
      case RaiseActionClass raise -> {
        var event = EventBuilder.from(raise.event).build();

        var parameters = new RaiseAction.Parameters(
            raise.name,
            event
        );

        return new RaiseAction(parameters);
      }
      case TimeoutActionClass timeout -> {
        var parameters = new TimeoutAction.Parameters(
            timeout.name
        );

        return new TimeoutAction(parameters);
      }
      case TimeoutResetActionClass timeoutReset -> {
        var parameters = new TimeoutResetAction.Parameters(
            timeoutReset.name
        );

        return new TimeoutResetAction(parameters);
      }
      default -> throw new IllegalStateException(String.format("Unexpected value: %s", actionClass.type));
    }
  }
}
