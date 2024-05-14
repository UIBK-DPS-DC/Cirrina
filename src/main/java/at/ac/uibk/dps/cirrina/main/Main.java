package at.ac.uibk.dps.cirrina.main;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.execution.object.context.Context;
import at.ac.uibk.dps.cirrina.execution.object.context.NatsContext;
import at.ac.uibk.dps.cirrina.execution.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.execution.object.event.NatsEventHandler;
import at.ac.uibk.dps.cirrina.execution.scheduler.RoundRobinRuntimeScheduler;
import at.ac.uibk.dps.cirrina.execution.scheduler.RuntimeScheduler;
import at.ac.uibk.dps.cirrina.main.MainDistributed.DistributedArgs;
import at.ac.uibk.dps.cirrina.main.MainShared.SharedArgs;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;


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
    // Set up logging
    setupLogging();

    // Construct shared arguments
    final var args = new Args();

    // Construct actionCommand arguments
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

      // Acquire requested actionCommand
      final var command = jc.getParsedCommand();

      if (command == null) {
        throw CirrinaException.from("Provide either distributed or shared ");
      }

      // Instantiate either the distributed or shared runtime main
      switch (command) {
        case "distributed":
          new MainDistributed(args, distributedArgs).run();
          break;
        case "shared":
          new MainShared(args, sharedArgs).run();
          break;
        default:
          throw CirrinaException.from("Unknown actionCommand '%s'", command);
      }
    } catch (CirrinaException e) {
      logger.error("Failed to run runtime: {}", e.getMessage());
    } catch (ParameterException e) {
      logger.error(e.getMessage());
    }
  }

  private static void setupLogging() {
    final var loggerContext = (LoggerContext) LogManager.getContext(false);
    final var loggerConfig = loggerContext.getConfiguration().getLoggerConfig(logger.getName());

    // Set log level
    loggerConfig.setLevel(Level.INFO);
    loggerContext.updateLoggers();
  }

  /**
   * Run the runtime.
   *
   * @throws CirrinaException In case of an error during execution or initialization.
   */
  public abstract void run() throws CirrinaException;

  /**
   * Constructs a new runtime scheduler according to the provided arguments.
   *
   * @return Runtime scheduler.
   * @throws CirrinaException If the runtime scheduler could not be constructed.
   */
  protected RuntimeScheduler newRuntimeScheduler() throws CirrinaException {
    switch (args.scheduler) {
      case RoundRobin -> {
        return new RoundRobinRuntimeScheduler();
      }
    }

    throw CirrinaException.from("Unknown scheduler '%s'", args.scheduler);
  }

  /**
   * Constructs a new event handler according to the provided arguments.
   *
   * @return Event handler.
   * @throws CirrinaException If the event handler could not be constructed.
   */
  protected EventHandler newEventHandler() throws CirrinaException {
    switch (args.eventHandler) {
      case Nats -> {
        return newNatsEventHandler();
      }
    }

    throw CirrinaException.from("Unknown event handler '%s'", args.eventHandler);
  }

  /**
   * Constructs a new NATS event handler according to the provided arguments.
   *
   * @return Event handler.
   * @throws CirrinaException If the event handler could not be constructed.
   */
  private NatsEventHandler newNatsEventHandler() throws CirrinaException {
    return new NatsEventHandler(args.natsEventHandlerArgs.natsUrl);
  }

  /**
   * Constructs a new persistent context according to the provided arguments.
   *
   * @return Persistent context.
   * @throws CirrinaException If the persistent context could not be constructed.
   */
  protected Context newPersistentContext() throws CirrinaException {
    switch (args.persistentContext) {
      case Nats -> {
        return newNatsPersistentContext();
      }
    }

    throw CirrinaException.from("Unknown persistent context '%s'", args.eventHandler);
  }

  /**
   * Constructs a new NATS persistent context according to the provided arguments.
   *
   * @return Persistent context.
   * @throws CirrinaException If the persistent context could not be constructed.
   */
  private NatsContext newNatsPersistentContext() throws CirrinaException {
    return new NatsContext(args.natsPersistentContextArgs.natsUrl, args.natsPersistentContextArgs.bucketName);
  }

  /**
   * Constructs a new Curator framework according to the provided arguments.
   *
   * @return Curator framework.
   * @throws CirrinaException In case a connection to ZooKeeper could not be made.
   */
  public CuratorFramework newCuratorFramework() throws CirrinaException {
    return CuratorFrameworkFactory.builder()
        .connectString(args.zooKeeperArgs.connectString)
        .retryPolicy(new ExponentialBackoffRetry(1000, 3))
        .connectionTimeoutMs(args.zooKeeperArgs.timeoutInMs)
        .sessionTimeoutMs(args.zooKeeperArgs.sessionTimeoutInMs)
        .build();
  }

  /**
   * Returns the OpenTelemetry SDK instance.
   *
   * @return OpenTelemetry SDK.
   */
  protected OpenTelemetry getOpenTelemetry() {
    return AutoConfiguredOpenTelemetrySdk.initialize()
        .getOpenTelemetrySdk();
  }

  /**
   * NATS event handler-specific arguments.
   */
  public final static class NatsEventHandlerArgs {

    @Parameter(names = {"--nats-event-handler-url"})
    private String natsUrl = "nats://localhost:4222/";
  }

  /**
   * NATS persistent context-specific arguments.
   */
  public final static class NatsPersistentContextArgs {

    @Parameter(names = {"--nats-persistent-context-url"})
    private String natsUrl = "nats://localhost:4222/";

    @Parameter(names = {"--nats-persistent-context-bucket-name"})
    private String bucketName = "persistent";
  }

  /**
   * ZooKeeper-specific arguments.
   */
  public final static class ZooKeeperArgs {

    @Parameter(names = "--zookeeper-connect-string")
    private String connectString = "localhost:2181";

    @Parameter(names = "--zookeeper-timeout-ms")
    private int timeoutInMs = 3000;

    @Parameter(names = "--zookeeper-session-timeout-ms")
    private int sessionTimeoutInMs = 3000;
  }

  /**
   * Shared arguments.
   */
  public final static class Args {

    @ParametersDelegate
    private final NatsEventHandlerArgs natsEventHandlerArgs = new NatsEventHandlerArgs();

    @ParametersDelegate
    private final NatsPersistentContextArgs natsPersistentContextArgs = new NatsPersistentContextArgs();

    @ParametersDelegate
    private final ZooKeeperArgs zooKeeperArgs = new ZooKeeperArgs();

    @Parameter(names = {"--scheduler", "-s"})
    private Scheduler scheduler = Scheduler.RoundRobin;

    @Parameter(names = {"--event-handler", "-e"})
    private EventHandler eventHandler = EventHandler.Nats;

    @Parameter(names = {"--persistent-context", "-p"})
    private PersistentContext persistentContext = PersistentContext.Nats;

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