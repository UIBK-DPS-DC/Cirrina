package at.ac.uibk.dps.cirrina.runtime.main;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.runtime.shared.SharedRuntime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main shared, is the entry-point to the shared runtime system.
 */
public final class MainShared extends Main {

  private static final Logger logger = LogManager.getLogger();

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
   *
   * @throws CirrinaException In case of an error during execution or initialization.
   */
  public void run() throws CirrinaException {
    final var runtimeScheduler = newRuntimeScheduler();

    try (final var eventHandler = newEventHandler()) {
      try (final var persistentContext = newPersistentContext()) {
        new SharedRuntime(runtimeScheduler, eventHandler, persistentContext);
      }
    } catch (Exception e) {
      throw CirrinaException.from("Failed to run shared runtime: %s", e);
    }
  }

  /**
   * The shared runtime-specific arguments.
   */
  public static class SharedArgs {

  }
}
