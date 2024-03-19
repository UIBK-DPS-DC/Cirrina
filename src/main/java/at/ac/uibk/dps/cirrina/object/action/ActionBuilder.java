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
import java.util.List;

/**
 * Action builder, used to build action objects.
 */
public final class ActionBuilder {

  /**
   * The action class to build from.
   */
  private final ActionClass actionClass;

  /**
   * Initializes an action builder.
   *
   * @param actionClass Action class.
   */
  private ActionBuilder(ActionClass actionClass) {
    this.actionClass = actionClass;
  }

  /**
   * Creates an action builder.
   *
   * @param actionClass Action class.
   * @return Action builder.
   */
  public static ActionBuilder from(ActionClass actionClass) {
    return new ActionBuilder(actionClass);
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
        return new AssignAction(
            assign.name,
            assign.variable.name,
            assign.variable.value.expression
        );
      }
      case CreateActionClass create -> {
        var contextVariable = ContextVariableBuilder.from(create.variable).build();
        return new CreateAction(
            create.name,
            contextVariable,
            create.isPersistent
        );
      }
      case InvokeActionClass invoke -> {
        return new InvokeAction(
            invoke.name,
            invoke.serviceType,
            invoke.isLocal,
            buildVariableList(invoke.input),
            buildEvents(invoke.done)
        );
      }
      case MatchActionClass match -> {
        return new MatchAction(match.name);
      }
      case RaiseActionClass raise -> {
        var event = EventBuilder.from(raise.event).build();
        return new RaiseAction(
            raise.name,
            event
        );
      }
      case TimeoutActionClass timeout -> {
        return new TimeoutAction(timeout.name);
      }
      case TimeoutResetActionClass timeoutReset -> {
        return new TimeoutResetAction(timeoutReset.name);
      }
      default -> throw new IllegalStateException(String.format("Unexpected value: %s", actionClass.type));
    }
  }
}
