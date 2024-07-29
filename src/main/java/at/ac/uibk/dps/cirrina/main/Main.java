package at.ac.uibk.dps.cirrina.main;

import at.ac.uibk.dps.cirrina.execution.object.context.Context;
import at.ac.uibk.dps.cirrina.execution.object.context.NatsContext;
import at.ac.uibk.dps.cirrina.execution.object.event.EventHandler;
import at.ac.uibk.dps.cirrina.execution.object.event.NatsEventHandler;
import at.ac.uibk.dps.cirrina.execution.scheduler.RoundRobinRuntimeScheduler;
import at.ac.uibk.dps.cirrina.execution.scheduler.RuntimeScheduler;
import at.ac.uibk.dps.cirrina.runtime.OnlineRuntime;
import at.ac.uibk.dps.cirrina.utils.Id;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.ResourceAttributes;
import java.io.IOException;
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
public class Main {

  /**
   * Main logger.
   */
  private static final Logger logger = LogManager.getLogger();

  /**
   * Runtime ID.
   */
  private final Id runtimeId = new Id();

  /**
   * Shared arguments.
   */
  private final Args args;

  /**
   * Initializes this main object.
   *
   * @param args The arguments to the main object.
   */
  private Main(Args args) {
    this.args = args;
  }

  public static void main(String... argv) {
    // Set up logging
    setupLogging();

    // Construct shared arguments
    final var args = new Args();

    // Construct argument parser
    final var jc = JCommander.newBuilder()
        .addObject(args)
        .build();

    try {
      // Parse arguments
      jc.parse(argv);

      // Instantiate the runtime main
      new Main(args).run();
    } catch (ParameterException e) {
      logger.error(e.getMessage());
    }

    logger.error("Runtime has stopped");
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
   */
  public void run() {
    // Connect to event system
    try (final var eventHandler = newEventHandler()) {
      eventHandler.subscribe(NatsEventHandler.GLOBAL_SOURCE, "*");
      eventHandler.subscribe(NatsEventHandler.PERIPHERAL_SOURCE, "*");

      // Connect to persistent context system
      try (final var persistentContext = newPersistentContext()) {
        // Connect to coordination system
        try (final var curatorFramework = newCuratorFramework()) {
          curatorFramework.start();

          // Acquire OpenTelemetry instance
          final var openTelemetry = getOpenTelemetry();

          // Create the shared runtime
          final var runtime = new OnlineRuntime(
              args.name,
              eventHandler,
              persistentContext,
              openTelemetry,
              curatorFramework,
              args.deleteJob);

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
   * Constructs a new runtime scheduler according to the provided arguments.
   *
   * @return Runtime scheduler.
   * @throws IllegalArgumentException If the scheduler provided is not known.
   */
  protected RuntimeScheduler newRuntimeScheduler() throws IllegalArgumentException {
    switch (args.scheduler) {
      case RoundRobin -> {
        return new RoundRobinRuntimeScheduler();
      }
    }

    throw new IllegalArgumentException("Unknown scheduler '%s'".formatted(args.scheduler));
  }

  /**
   * Constructs a new event handler according to the provided arguments.
   *
   * @return Event handler.
   * @throws IOException              If the event handler could not be constructed.
   * @throws IllegalArgumentException If the event handler provided is not known.
   */
  protected EventHandler newEventHandler() throws IOException, IllegalArgumentException {
    switch (args.eventHandler) {
      case Nats -> {
        return newNatsEventHandler();
      }
    }

    throw new IllegalArgumentException("Unknown event handler '%s'".formatted(args.eventHandler));
  }

  /**
   * Constructs a new NATS event handler according to the provided arguments.
   *
   * @return Event handler.
   * @throws IOException If the event handler could not be constructed.
   */
  private NatsEventHandler newNatsEventHandler() throws IOException {
    return new NatsEventHandler(args.natsEventHandlerArgs.natsUrl);
  }

  /**
   * Constructs a new persistent context according to the provided arguments.
   *
   * @return Persistent context.
   * @throws IOException              If the event handler could not be constructed.
   * @throws IllegalArgumentException If the persistent context provided is not known.
   */
  protected Context newPersistentContext() throws IOException, IllegalArgumentException {
    switch (args.persistentContext) {
      case Nats -> {
        return newNatsPersistentContext();
      }
    }

    throw new IllegalArgumentException("Unknown persistent context '%s'".formatted(args.eventHandler));
  }

  /**
   * Constructs a new NATS persistent context according to the provided arguments.
   *
   * @return Persistent context.
   * @throws IOException If the persistent context could not be constructed.
   */
  private NatsContext newNatsPersistentContext() throws IOException {
    return new NatsContext(false, args.natsPersistentContextArgs.natsUrl, args.natsPersistentContextArgs.bucketName);
  }

  /**
   * Constructs a new Curator framework according to the provided arguments.
   *
   * @return Curator framework.
   */
  public CuratorFramework newCuratorFramework() {
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
    SpanExporter spanExporter = OtlpGrpcSpanExporter.builder().setEndpoint("http://localhost:4317").build();
    SpanExporter jaegerSpanExporter = JaegerGrpcSpanExporter.builder().setEndpoint("http://localhost:14250").build();

    SpanProcessor otlpSpanProcessor = BatchSpanProcessor.builder(spanExporter).build();
    SpanProcessor jaegerSpanProcessor = BatchSpanProcessor.builder(jaegerSpanExporter).build();

    Resource resource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "cirrina"));

    SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(otlpSpanProcessor)
        .addSpanProcessor(jaegerSpanProcessor)
        .setResource(resource).build();

    MetricExporter metricExporter = OtlpGrpcMetricExporter.builder().setEndpoint("http://localhost:4317").build();

    PeriodicMetricReader metricReader = PeriodicMetricReader.builder(metricExporter).setInterval(java.time.Duration.ofSeconds(5)).build();

    SdkMeterProvider meterProvider = SdkMeterProvider.builder().setResource(resource).registerMetricReader(metricReader).build();

    OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setMeterProvider(meterProvider)
        .setPropagators(ContextPropagators.create(TextMapPropagator.composite(
            W3CTraceContextPropagator.getInstance()))).buildAndRegisterGlobal();

    return GlobalOpenTelemetry.get();
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

    @Parameter(names = {"--name", "-n"}, required = true)
    private String name;

    @Parameter(names = {"--delete-job", "-d"}, arity = 1)
    private boolean deleteJob = true;

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
