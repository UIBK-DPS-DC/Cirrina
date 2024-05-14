package at.ac.uibk.dps.cirrina.execution.object.action;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.ACTION_NAME_DOES_NOT_EXIST;
import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.TIMEOUT_ACTION_NAME_IS_NOT_PROVIDED;

import at.ac.uibk.dps.cirrina.classes.helper.ActionResolver;
import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.csml.description.action.ActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.AssignActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.CreateActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.InvokeActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.MatchActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.MatchCaseDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.RaiseActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.TimeoutActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.TimeoutResetActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.context.ContextVariableDescription;
import at.ac.uibk.dps.cirrina.csml.description.event.EventDescription;
import at.ac.uibk.dps.cirrina.execution.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.execution.object.context.ContextVariableBuilder;
import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.event.EventBuilder;
import at.ac.uibk.dps.cirrina.execution.object.expression.Expression;
import at.ac.uibk.dps.cirrina.execution.object.expression.ExpressionBuilder;
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
  private final ActionDescription actionDescription;

  /**
   * Action resolver, resolves action names or constructs action objects.
   */
  private final ActionResolver actionResolver;

  /**
   * Initializes an action builder.
   *
   * @param actionDescription Action class.
   */
  private ActionBuilder(ActionDescription actionDescription, ActionResolver actionResolver) {
    this.actionDescription = actionDescription;
    this.actionResolver = actionResolver;
  }

  /**
   * Creates an action builder.
   *
   * @param actionClass Action class.
   * @return Action builder.
   */
  public static ActionBuilder from(ActionDescription actionClass, ActionResolver actionResolver) {
    return new ActionBuilder(actionClass, actionResolver);
  }

  /**
   * Returns a list of variables.
   *
   * @param contextVariableDescriptions Variable classes.
   * @return Variables.
   */
  private static List<ContextVariable> buildVariableList(List<ContextVariableDescription> contextVariableDescriptions) {
    return contextVariableDescriptions.stream()
        .map(c -> ContextVariableBuilder.from(c).build())
        .toList();
  }

  /**
   * Returns a list of events.
   *
   * @param eventDescriptions Event classes.
   * @return Events.
   */
  private static List<Event> buildEvents(List<EventDescription> eventDescriptions) {
    return eventDescriptions.stream()
        .map(e -> EventBuilder.from(e).build())
        .toList();
  }

  /**
   * Returns a map of cases.
   *
   * @param cases Case classes.
   * @return Cases.
   */
  private Map<Expression, Action> buildCases(List<MatchCaseDescription> cases) {
    final Map<Expression, Action> ret = new HashMap<>();

    for (final var casee : cases) {
      ret.put(ExpressionBuilder.from(casee.casee).build(), actionResolver.tryResolve(casee.action)
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
    switch (actionDescription) {
      case AssignActionDescription assign -> {
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
      case CreateActionDescription create -> {
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
      case InvokeActionDescription invoke -> {
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
      case MatchActionDescription match -> {
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
      case RaiseActionDescription raise -> {
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
      case TimeoutActionDescription timeout -> {
        // Acquire the action name, for timeout actions, the name is always required
        final var name = timeout.name.orElseThrow(
            () -> new IllegalArgumentException(VerificationException.from(TIMEOUT_ACTION_NAME_IS_NOT_PROVIDED)));

        // Acquire the delay expression
        final var delayExpression = ExpressionBuilder.from(timeout.delay).build();

        // Acquire the timeout action
        final var timeoutAction = actionResolver.tryResolve(timeout.action)
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
      case TimeoutResetActionDescription timeoutReset -> {
        // Construct parameters
        final var parameters = new TimeoutResetAction.Parameters(
            timeoutReset.name,
            timeoutReset.action
        );

        // Construct the timeout reset action
        return new TimeoutResetAction(parameters);
      }
      default -> throw new IllegalStateException(String.format("Unexpected value: %s", actionDescription.type));
    }
  }
}
