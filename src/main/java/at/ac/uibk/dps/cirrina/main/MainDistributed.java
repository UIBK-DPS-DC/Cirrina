package at.ac.uibk.dps.cirrina.main;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main distributed, is the entry-point to the distributed runtime system.
 */
public final class MainDistributed extends Main {

  private static final Logger logger = LogManager.getLogger();

  private final DistributedArgs distributedArgs;

  /**
   * Initializes this main distributed object.
   *
   * @param args            The arguments to the main distributed object.
   * @param distributedArgs The distributed runtime-specific arguments to the main distributed object.
   */
  MainDistributed(Args args, DistributedArgs distributedArgs) {
    super(args);

    this.distributedArgs = distributedArgs;
  }

  /**
   * Run the runtime.
   *
   * @throws CirrinaException In case of an error during execution or initialization.
   */
  public void run() throws CirrinaException {

  }

  /**
   * The distributed runtime-specific arguments.
   */
  public static class DistributedArgs {

  }
}