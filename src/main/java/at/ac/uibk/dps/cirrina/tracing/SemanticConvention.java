package at.ac.uibk.dps.cirrina.tracing;

public class SemanticConvention {

  // Counter names
  public static final String COUNTER_EVENTS_RECEIVED = "cirrina.events.received";
  public static final String COUNTER_EVENTS_HANDLED = "cirrina.events.handled";
  public static final String COUNTER_INVOCATIONS = "cirrina.invocations";

  // Counter attributes
  public static final String COUNTER_ATTR_EVENT_CHANNEL = "cirrina.event.channel";

  // Gauge names
  public static final String GAUGE_ACTION_DATA_LATENCY = "cirrina.action.data_latency_ms";
  public static final String GAUGE_ACTION_INVOKE_LATENCY = "cirrina.action.invoke_latency_ms";
  public static final String GAUGE_ACTION_RAISE_LATENCY = "cirrina.action.raise_latency_ms";

  // Gauge attributes
  public static final String GAUGE_ATTR_DATA_OPERATION = "cirrina.data.operation";
  public static final String GAUGE_ATTR_DATA_LOCALITY = "cirrina.data.locality";
  public static final String GAUGE_ATTR_DATA_SIZE = "cirrina.data.size";

  public static final String GAUGE_ATTR_INVOCATION_LOCALITY = "cirrina.invocation.locality";

  // General attributes
  public static final String ATTR_STATE_MACHINE_ID = "cirrina.state_machine.id";
}
