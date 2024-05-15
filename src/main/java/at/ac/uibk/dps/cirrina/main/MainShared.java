package at.ac.uibk.dps.cirrina.main;

import at.ac.uibk.dps.cirrina.execution.object.event.NatsEventHandler;
import at.ac.uibk.dps.cirrina.runtime.OnlineRuntime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main shared, is the entry-point to the shared runtime system.
 */
public final class MainShared extends Main {

  /**
   * Main shared logger.
   */
  private static final Logger logger = LogManager.getLogger();

  /**
   * Shared runtime arguments.
   */
  private final SharedArgs sharedArgs;

  /**
   * Initializes this main shared object.
   *
   * @param args       The arguments to the main shared object.
   * @param sharedArgs The shared runtime-specific arguments to the main shared object.
   */
  MainShared(Args args, SharedArgs sharedArgs) {
    super(args);

    this.sharedArgs = sharedArgs;
  }

  /**
   * Run the runtime.
   */
  public void run() {
    // Connect to event system
    try (final var eventHandler = newEventHandler()) {
      eventHandler.subscribe(NatsEventHandler.GLOBAL_SOURCE, "*");

      // Connect to persistent context system
      try (final var persistentContext = newPersistentContext()) {
        // Connect to coordination system
        try (final var curatorFramework = newCuratorFramework()) {
          curatorFramework.start();

          // Acquire OpenTelemetry instance
          final var openTelemetry = getOpenTelemetry();

          // Create the shared runtime
          final var runtime = new OnlineRuntime(eventHandler, persistentContext, openTelemetry, curatorFramework);

          runtime.run();

          logger.info("Done running");
        }
      }
    } catch (InterruptedException e) {
      logger.info("Interrupted.");

      Thread.currentThread().interrupt();
    } catch (Exception e) {
      logger.error("Could not initialize the shared runtime", e);
    }
  }

  /**
   * The shared runtime-specific arguments.
   */
  public static class SharedArgs {

  }
}
