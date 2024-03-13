package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.objects.Event;
import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.actions.AssignAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.CreateAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.InvokeAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.MatchAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.RaiseAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.TimeoutAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.core.objects.context.Context.ContextVariable;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.ActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.AssignActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.CreateActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.InvokeActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.MatchActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.RaiseActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.TimeoutActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.TimeoutResetActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.context.ContextVariableClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.events.EventClass;
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
        .map(c -> new ContextVariable(c.name, c.value))
        .toList();
  }

  private static List<Event> buildEvents(List<EventClass> eventClasses) {
    return eventClasses.stream()
        .map(e -> new Event(e.name, e.channel, e.data))
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
      default -> throw new IllegalStateException("Unexpected value: " + actionClass.type);
    }
  }
}
