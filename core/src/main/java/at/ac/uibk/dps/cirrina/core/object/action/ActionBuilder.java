package at.ac.uibk.dps.cirrina.core.object.action;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.ACTION_NAME_DOES_NOT_EXIST;
import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.TIMEOUT_ACTION_NAME_IS_NOT_PROVIDED;

import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.lang.classes.action.ActionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.action.AssignActionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.action.CreateActionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.action.InvokeActionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.action.MatchActionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.action.MatchCase;
import at.ac.uibk.dps.cirrina.core.lang.classes.action.RaiseActionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.action.TimeoutActionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.action.TimeoutResetActionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.context.ContextVariableClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.event.EventClass;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariableBuilder;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.core.object.event.EventBuilder;
import at.ac.uibk.dps.cirrina.core.object.expression.Expression;
import at.ac.uibk.dps.cirrina.core.object.expression.ExpressionBuilder;
import at.ac.uibk.dps.cirrina.core.object.helper.ActionResolver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action builder, used to build action objects.
 */
public final class ActionBuilder {

  /**
   * The action class to build from.
   */
  private final ActionClass actionClass;

  /**
   * Action resolver, resolves action names or constructs action objects.
   */
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

  /**
   * Returns a list of variables.
   *
   * @param contextVariableClasses Variable classes.
   * @return Variables.
   */
  private static List<ContextVariable> buildVariableList(List<ContextVariableClass> contextVariableClasses) {
    return contextVariableClasses.stream()
        .map(c -> ContextVariableBuilder.from(c).build())
        .toList();
  }

  /**
   * Returns a list of events.
   *
   * @param eventClasses Event classes.
   * @return Events.
   */
  private static List<Event> buildEvents(List<EventClass> eventClasses) {
    return eventClasses.stream()
        .map(e -> EventBuilder.from(e).build())
        .toList();
  }

  /**
   * Returns a map of cases.
   *
   * @param cases Case classes.
   * @return Cases.
   */
  private Map<Expression, Action> buildCases(List<MatchCase> cases) {
    final Map<Expression, Action> ret = new HashMap<>();

    for (final var casee : cases) {
      ret.put(ExpressionBuilder.from(casee.casee).build(), actionResolver.resolve(casee.action)
          .orElseThrow(() -> new IllegalArgumentException(VerificationException.from(ACTION_NAME_DOES_NOT_EXIST, casee.action))));
    }

    return ret;
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
        // Acquire the context variable
        final var contextVariable = ContextVariableBuilder.from(assign.variable).build();

        // Construct parameters
        final var parameters = new AssignAction.Parameters(
            assign.name,
            contextVariable
        );

        // Construct the assign action
        return new AssignAction(parameters);
      }
      case CreateActionClass create -> {
        final var contextVariable = ContextVariableBuilder.from(create.variable).build();

        // Construct parameters
        final var parameters = new CreateAction.Parameters(
            create.name,
            contextVariable,
            create.isPersistent
        );

        // Construct the create action
        return new CreateAction(parameters);
      }
      case InvokeActionClass invoke -> {
        // Acquire the input variables
        final var input = buildVariableList(invoke.input);

        // Acquire the done events
        final var done = buildEvents(invoke.done);

        // Construct parameters
        final var parameters = new InvokeAction.Parameters(
            invoke.name,
            invoke.serviceType,
            invoke.isLocal,
            input,
            done,
            invoke.output
        );

        // Construct the invoke action
        return new InvokeAction(parameters);
      }
      case MatchActionClass match -> {
        // Acquire the value expression
        final var valueExpression = ExpressionBuilder.from(match.value).build();

        // Acquire the cases
        final var cases = buildCases(match.cases);

        // Construct parameters
        final var parameters = new MatchAction.Parameters(
            match.name,
            valueExpression,
            cases
        );

        // Construct the match action
        return new MatchAction(parameters);
      }
      case RaiseActionClass raise -> {
        // Acquire the event
        final var event = EventBuilder.from(raise.event).build();

        // Construct parameters
        final var parameters = new RaiseAction.Parameters(
            raise.name,
            event
        );

        // Construct the raise action
        return new RaiseAction(parameters);
      }
      case TimeoutActionClass timeout -> {
        // Acquire the action name, for timeout actions, the name is always required
        final var name = timeout.name.orElseThrow(
            () -> new IllegalArgumentException(VerificationException.from(TIMEOUT_ACTION_NAME_IS_NOT_PROVIDED)));

        // Acquire the delay expression
        final var delayExpression = ExpressionBuilder.from(timeout.delay).build();

        // Acquire the timeout action
        final var timeoutAction = actionResolver.resolve(timeout.action)
            .orElseThrow(() -> new IllegalArgumentException(VerificationException.from(ACTION_NAME_DOES_NOT_EXIST, timeout.action)));

        // Construct parameters
        final var parameters = new TimeoutAction.Parameters(
            name,
            delayExpression,
            timeoutAction
        );

        // Construct the timeout action
        return new TimeoutAction(parameters);
      }
      case TimeoutResetActionClass timeoutReset -> {
        // Construct parameters
        final var parameters = new TimeoutResetAction.Parameters(
            timeoutReset.name,
            timeoutReset.action
        );

        // Construct the timeout reset action
        return new TimeoutResetAction(parameters);
      }
      default -> throw new IllegalStateException(String.format("Unexpected value: %s", actionClass.type));
    }
  }
}
