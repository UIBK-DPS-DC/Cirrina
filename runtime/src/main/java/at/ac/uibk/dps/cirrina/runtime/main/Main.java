package at.ac.uibk.dps.cirrina.runtime.main;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.NatsContext;
import at.ac.uibk.dps.cirrina.core.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.core.object.event.NatsEventHandler;
import at.ac.uibk.dps.cirrina.runtime.main.MainDistributed.DistributedArgs;
import at.ac.uibk.dps.cirrina.runtime.main.MainShared.SharedArgs;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RoundRobinRuntimeScheduler;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RuntimeScheduler;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Main, is the entry-point to the runtime system.
 */
public abstract class Main {

  private static final Logger logger = LogManager.getLogger();

  private final Args args;

  /**
   * Initializes this main object.
   *
   * @param args The arguments to the main object.
   */
  protected Main(Args args) {
    this.args = args;
  }

  public static void main(String... argv) {
    // Construct shared arguments
    final var args = new Args();

    // Construct command arguments
    final var distributedArgs = new DistributedArgs();
    final var sharedArgs = new SharedArgs();

    // Construct argument parser
    final var jc = JCommander.newBuilder()
        .addObject(args)
        .addCommand("distributed", distributedArgs)
        .addCommand("shared", sharedArgs)
        .build();

    try {
      // Parse arguments
      jc.parse(argv);

      // Acquire requested command
      final var command = jc.getParsedCommand();

      // Instantiate either the distributed or shared runtime main
      switch (command) {
        case "distributed":
          new MainDistributed(args, distributedArgs).run();
          break;
        case "shared":
          new MainShared(args, sharedArgs).run();
          break;
        default:
          throw RuntimeException.from("Unknown command '%s'", command);
      }
    } catch (RuntimeException e) {
      logger.error("Failed to run runtime: {}", e.getMessage());
    } catch (ParameterException e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * Run the runtime.
   *
   * @throws RuntimeException In case of an error during execution or initialization.
   */
  public abstract void run() throws RuntimeException;

  /**
   * Constructs a new runtime scheduler according to the requested arguments.
   *
   * @return Runtime scheduler.
   * @throws RuntimeException If the runtime scheduler could not be constructed.
   */
  public RuntimeScheduler newRuntimeScheduler() throws RuntimeException {
    switch (args.scheduler) {
      case RoundRobin -> {
        return new RoundRobinRuntimeScheduler();
      }
    }

    throw RuntimeException.from("Unknown scheduler '%s'", args.scheduler);
  }

  /**
   * Constructs a new event handler according to the requested arguments.
   *
   * @return Event handler.
   * @throws RuntimeException If the event handler could not be constructed.
   */
  public EventHandler newEventHandler() throws RuntimeException {
    switch (args.eventHandler) {
      case Nats -> {
        return newNatsEventHandler();
      }
    }

    throw RuntimeException.from("Unknown event handler '%s'", args.eventHandler);
  }

  /**
   * Constructs a new persistent context according to the requested arguments.
   *
   * @return Persistent context.
   * @throws RuntimeException If the persistent context could not be constructed.
   */
  public Context newPersistentContext() throws RuntimeException {
    switch (args.persistentContext) {
      case Nats -> {
        return newNatsPersistentContext();
      }
    }

    throw RuntimeException.from("Unknown persistent context '%s'", args.eventHandler);
  }

  private NatsEventHandler newNatsEventHandler() throws RuntimeException {
    return new NatsEventHandler(args.natsEventHandlerArgs.natsUrl);
  }

  private NatsContext newNatsPersistentContext() throws RuntimeException {
    return new NatsContext(args.natsPersistentContextArgs.natsUrl, args.natsPersistentContextArgs.bucketName);
  }

  /**
   * NATS event handler-specific arguments.
   */
  public final static class NatsEventHandlerArgs {

    @Parameter(names = {"--nats-event-handler-url"})
    private String natsUrl;
  }

  /**
   * NATS persistent context-specific arguments.
   */
  public final static class NatsPersistentContextArgs {

    @Parameter(names = {"--nats-persistent-context-url"})
    private String natsUrl;

    @Parameter(names = {"--nats-persistent-context-bucket-name"})
    private String bucketName;
  }

  /**
   * Shared arguments.
   */
  public final static class Args {

    @ParametersDelegate
    private final NatsEventHandlerArgs natsEventHandlerArgs = new NatsEventHandlerArgs();
    @ParametersDelegate
    private final NatsPersistentContextArgs natsPersistentContextArgs = new NatsPersistentContextArgs();
    @Parameter(names = {"--scheduler", "-s"}, required = true)
    private Scheduler scheduler;
    @Parameter(names = {"--event-handler", "-e"}, required = true)
    private EventHandler eventHandler;
    @Parameter(names = {"--persistent-context", "-p"}, required = true)
    private PersistentContext persistentContext;

    enum Scheduler {
      RoundRobin
    }

    enum EventHandler {
      Nats
    }

    enum PersistentContext {
      Nats
    }
  }
}
