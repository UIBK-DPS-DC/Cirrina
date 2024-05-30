package at.ac.uibk.dps.cirrina.tracing;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.COUNTER_ATTR_EVENT_CHANNEL;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.COUNTER_ATTR_TRANSITION_TYPE;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import java.util.HashMap;
import java.util.Map;

public class Counters {

  private final Map<String, LongCounter> counters = new HashMap<>();

  private final Meter meter;

  public Counters(Meter meter) {
    this.meter = meter;
  }

  public static Attributes attributesForEvent(String eventChannel) {
    return Attributes.builder()
        .put(COUNTER_ATTR_EVENT_CHANNEL, eventChannel)
        .build();
  }

  public static Attributes attributesForTransition(String transitionType) {
    return Attributes.builder()
        .put(COUNTER_ATTR_TRANSITION_TYPE, transitionType)
        .build();
  }

  public void addCounter(String name) {
    counters.put(name, meter.counterBuilder(name).build());
  }

  public LongCounter getCounter(String name) {
    return counters.get(name);
  }
}
