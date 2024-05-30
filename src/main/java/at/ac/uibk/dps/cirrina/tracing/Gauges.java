package at.ac.uibk.dps.cirrina.tracing;

import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.COUNTER_ATTR_DATA_LOCALITY;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.COUNTER_ATTR_DATA_OPERATION;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.COUNTER_ATTR_DATA_SIZE;
import static at.ac.uibk.dps.cirrina.tracing.SemanticConvention.COUNTER_ATTR_INVOCATION_LOCALITY;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.Meter;
import java.util.HashMap;
import java.util.Map;

public class Gauges {

  private final Map<String, DoubleGauge> gauges = new HashMap<>();

  private final Meter meter;

  public Gauges(Meter meter) {
    this.meter = meter;
  }

  public static Attributes attributesForData(String operation, String dataLocality, int dataSize) {
    return Attributes.builder()
        .put(COUNTER_ATTR_DATA_OPERATION, operation)
        .put(COUNTER_ATTR_DATA_LOCALITY, dataLocality)
        .put(COUNTER_ATTR_DATA_SIZE, dataSize)
        .build();
  }

  public static Attributes attributesForInvocation(String serviceLocality) {
    return Attributes.builder()
        .put(COUNTER_ATTR_INVOCATION_LOCALITY, serviceLocality)
        .build();
  }

  public void addGauge(String name) {
    gauges.put(name, meter.gaugeBuilder(name).build());
  }

  public DoubleGauge getGauge(String name) {
    return gauges.get(name);
  }
}
