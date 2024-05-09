package at.ac.uibk.dps.cirrina.execution.runtime;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import org.junit.jupiter.api.BeforeAll;

public class RuntimeTest {

  protected static OpenTelemetry openTelemetry;

  @BeforeAll
  public static void setUpRuntimeTest() {
    openTelemetry = AutoConfiguredOpenTelemetrySdk.builder()
        .build()
        .getOpenTelemetrySdk();
  }
}
