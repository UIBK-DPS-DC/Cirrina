package at.ac.uibk.dps.cirrina.tracing;

public class SemanticConvention {

  public static String SPAN_ACTION_ASSIGN_COMMAND_EXECUTE = "actionAssignCommandExecute";

  public static String SPAN_ACTION_CREATE_COMMAND_EXECUTE = "actionCreateCommandExecute";

  public static String SPAN_ACTION_INVOKE_COMMAND_EXECUTE = "actionInvokeCommandExecute";

  public static String SPAN_ACTION_MATCH_COMMAND_EXECUTE = "actionMatchCommandExecute";

  public static String SPAN_ACTION_RAISE_COMMAND_EXECUTE = "actionRaiseCommandExecute";

  public static String SPAN_ACTION_TIMEOUT_COMMAND_EXECUTE = "actionTimeoutCommandExecute";

  public static String SPAN_NEW_INSTANCE = "newInstance";

  public static String SPAN_RUN = "run";

  public static String SPAN_DO_EXIT = "doExit";

  public static String SPAN_DO_TRANSITION = "doTransition";

  public static String SPAN_DO_ENTER = "doEnter";

  public static String SPAN_TIMEOUT = "timeout";

  public static String SPAN_TRANSITION = "transition";

  public static String EVENT_RECEIVED_EVENT = "receivedEvent";

  public static String METRIC_EXECUTED_ACTIONS = "executed_actions";

  public static String METRIC_RECEIVED_EVENTS = "received_events";

  public static String METRIC_HANDLED_EVENTS = "handled_events";

  public static String METRIC_EXTERNAL_TRANSITIONS = "external_transitions";

  public static String METRIC_INTERNAL_TRANSITIONS = "internal_transitions";

  public static String ATTR_SERVICE_NAME = "service.name";
  public static String ATTR_SERVICE_COST = "service.cost";
  public static String ATTR_SERVICE_PERFORMANCE = "service.performance";
  public static String ATTR_SERVICE_IS_LOCAL = "service.isLocal";

  public static String ATTR_EVENT_ID = "event.id";
  public static String ATTR_EVENT_NAME = "event.name";
  public static String ATTR_EVENT_CHANNEL = "event.channel";

  public static String ATTR_VARIABLE_NAME = "variable.name";

  public static String ATTR_STATE_MACHINE_NAME = "stateMachine.name";
  public static String ATTR_STATE_MACHINE_PARENT_ID = "stateMachine.parentID";

  public static String ATTR_ACTION_NAME = "action.name";

  public static String ATTR_TRANSITION_TARGET_STATE_NAME = "transition.targetStateName";
  public static String ATTR_TRANSITION_TYPE = "transition.type";
}
