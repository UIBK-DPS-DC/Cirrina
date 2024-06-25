package at.ac.uibk.dps.cirrina.utils;

import java.time.Duration;
import java.time.Instant;

public class Time {

  private static final Instant processStartTime = Instant.now();

  public static double timeInMillisecondsSinceEpoch() {
    final var now = Instant.now();
    return now.getEpochSecond() * 1.0e3 + now.getNano() / 1.0e6;
  }

  public static double timeInMillisecondsSinceStart() {
    Instant now = Instant.now();
    return Duration.between(processStartTime, now).toNanos() / 1_000_000.0;
  }
}
