package at.ac.uibk.dps.cirrina.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TimeTest {

  @Test
  void testTimeInMillisecondsSinceEpoch() throws InterruptedException {
    final var a = Time.timeInMillisecondsSinceEpoch();

    Thread.sleep(100);

    final var b = Time.timeInMillisecondsSinceEpoch();

    assertTrue(b > a);
  }

  @Test
  void testTimeInMillisecondsSinceStart() throws InterruptedException {
    final var a = Time.timeInMillisecondsSinceStart();

    Thread.sleep(100);

    final var b = Time.timeInMillisecondsSinceStart();

    assertTrue(b > a);
  }
}
