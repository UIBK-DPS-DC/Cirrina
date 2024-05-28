package at.ac.uibk.dps.cirrina.tracing;

public class SemanticConvention {

  // Span names
  public static final String SPAN_ACTION_ASSIGN_COMMAND_EXECUTE = "cirrina.action.assign_command_execute";
  public static final String SPAN_ACTION_CREATE_COMMAND_EXECUTE = "cirrina.action.create_command_execute";
  public static final String SPAN_ACTION_INVOKE_COMMAND_EXECUTE = "cirrina.action.invoke_command_execute";
  public static final String SPAN_ACTION_MATCH_COMMAND_EXECUTE = "cirrina.action.match_command_execute";
  public static final String SPAN_ACTION_RAISE_COMMAND_EXECUTE = "cirrina.action.raise_command_execute";
  public static final String SPAN_ACTION_TIMEOUT_COMMAND_EXECUTE = "cirrina.action.timeout_command_execute";

  public static final String SPAN_INSTANCE_NEW = "cirrina.instance.new";

  public static final String SPAN_DO_EXIT = "cirrina.do.exit";
  public static final String SPAN_DO_TRANSITION = "cirrina.do.transition";
  public static final String SPAN_DO_ENTER = "cirrina.do.enter";

  public static final String SPAN_TIMEOUT = "cirrina.timeout";
  public static final String SPAN_TRANSITION = "cirrina.transition";

  public static final String EVENT_STATE_SWITCHED = "cirrina.state.switched";

  // Metric names
  public static final String METRIC_ACTIONS_EXECUTED = "cirrina.actions.executed";
  public static final String METRIC_ACTION_ASSIGN_LATENCY = "cirrina.action.assign_latency_ms";
  public static final String METRIC_ACTION_CREATE_LATENCY = "cirrina.action.create_latency_ms";
  public static final String METRIC_ACTION_INVOKE_LATENCY = "cirrina.action.invoke_latency_ms";
  public static final String METRIC_ACTION_RAISE_LATENCY = "cirrina.action.raise_latency_ms";

  public static final String METRIC_EVENTS_RECEIVED = "cirrina.events.received";

  public static final String METRIC_TRANSITIONS_HANDLED = "cirrina.transitions.handled";
  public static final String METRIC_TRANSITION_LATENCY = "cirrina.transition.latency_ms";

  public static final String METRIC_UPTIME = "cirrina.uptime_s";

  // Attribute names
  public static final String ATTR_SERVICE_NAME = "cirrina.service.name";
  public static final String ATTR_SERVICE_IS_LOCAL = "cirrina.service.is_local";

  public static final String ATTR_EVENT_ID = "cirrina.event.id";
  public static final String ATTR_EVENT_NAME = "cirrina.event.name";
  public static final String ATTR_EVENT_CHANNEL = "cirrina.event.channel";

  public static final String ATTR_VARIABLE_NAME = "cirrina.variable.name";

  public static final String ATTR_STATE_MACHINE_ID = "cirrina.state_machine.id";
  public static final String ATTR_STATE_MACHINE_NAME = "cirrina.state_machine.name";
  public static final String ATTR_STATE_MACHINE_PARENT_ID = "cirrina.state_machine.parent_id";

  public static final String ATTR_ACTION_NAME = "cirrina.action.name";

  public static final String ATTR_TRANSITION_TARGET_STATE_NAME = "cirrina.transition.target_state_name";
  public static final String ATTR_TRANSITION_TYPE = "cirrina.transition.type";
  public static final String ATTR_TRANSITION_STATE_NAME = "cirrina.transition.state_name";
}
