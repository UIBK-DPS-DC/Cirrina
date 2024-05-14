package at.ac.uibk.dps.cirrina.tracing;

public class SemanticConvention {

  public static String SPAN_ACTION_ASSIGN_COMMAND_EXECUTE = "cirrina.action.assignCommandExecute";

  public static String SPAN_ACTION_CREATE_COMMAND_EXECUTE = "cirrina.action.createCommandExecute";

  public static String SPAN_ACTION_INVOKE_COMMAND_EXECUTE = "cirrina.action.invokeCommandExecute";

  public static String SPAN_ACTION_MATCH_COMMAND_EXECUTE = "cirrina.action.matchCommandExecute";

  public static String SPAN_ACTION_RAISE_COMMAND_EXECUTE = "cirrina.action.raiseCommandExecute";

  public static String SPAN_ACTION_TIMEOUT_COMMAND_EXECUTE = "cirrina.action.TimeoutCommandExecute";

  public static String SPAN_NEW_INSTANCE = "cirrina.newInstance";

  public static String SPAN_RUN = "cirrina.run";

  public static String SPAN_DO_EXIT = "cirrina.do.exit";

  public static String SPAN_DO_TRANSITION = "cirrina.do.transition";

  public static String SPAN_DO_ENTER = "cirrina.do.enter";

  public static String SPAN_TIMEOUT = "cirrina.timeout";

  public static String SPAN_TRANSITION = "cirrina.transition";

  public static String EVENT_RECEIVED_EVENT = "cirrina.receivedEvent";

  public static String METRIC_EXECUTED_ACTIONS = "cirrina.executed_actions";

  public static String METRIC_RECEIVED_EVENTS = "cirrina.received_events";

  public static String METRIC_HANDLED_EVENTS = "cirrina.handled_events";

  public static String METRIC_EXTERNAL_TRANSITIONS = "cirrina.external_transitions";

  public static String METRIC_INTERNAL_TRANSITIONS = "cirrina.internal_transitions";

  public static String METRIC_UPTIME = "cirrina.uptime";

  public static String ATTR_SERVICE_NAME = "cirrina.service.name";
  public static String ATTR_SERVICE_COST = "cirrina.service.cost";
  public static String ATTR_SERVICE_PERFORMANCE = "cirrina.service.performance";
  public static String ATTR_SERVICE_IS_LOCAL = "cirrina.service.isLocal";

  public static String ATTR_EVENT_ID = "cirrina.event.id";
  public static String ATTR_EVENT_NAME = "cirrina.event.name";
  public static String ATTR_EVENT_CHANNEL = "cirrina.event.channel";

  public static String ATTR_VARIABLE_NAME = "cirrina.variable.name";

  public static String ATTR_STATE_MACHINE_NAME = "cirrina.stateMachine.name";
  public static String ATTR_STATE_MACHINE_PARENT_ID = "cirrina.stateMachine.parentID";

  public static String ATTR_ACTION_NAME = "cirrina.action.name";

  public static String ATTR_TRANSITION_TARGET_STATE_NAME = "cirrina.transition.targetStateName";
  public static String ATTR_TRANSITION_TYPE = "cirrina.transition.type";
}
