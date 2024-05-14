package at.ac.uibk.dps.cirrina.main;

import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClass;
import at.ac.uibk.dps.cirrina.classes.collaborativestatemachine.CollaborativeStateMachineClassBuilder;
import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.csml.description.CollaborativeStateMachineDescription;
import at.ac.uibk.dps.cirrina.execution.object.event.NatsEventHandler;
import at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachineId;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.execution.service.ServicesImplementationBuilder;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationsDescription;
import at.ac.uibk.dps.cirrina.io.description.DescriptionParser;
import at.ac.uibk.dps.cirrina.runtime.OfflineRuntime;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
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
    // Connect to event system
    try (final var eventHandler = newEventHandler()) {
      eventHandler.subscribe(NatsEventHandler.GLOBAL_SOURCE, "*");

      // Connect to persistent context system
      try (final var persistentContext = newPersistentContext()) {
        // Connect to coordination system
        try (final var curatorFramework = newCuratorFramework()) {
          // Acquire OpenTelemetry instance
          final var openTelemetry = getOpenTelemetry();

          // Create the shared runtime
          final var runtime = new OfflineRuntime(eventHandler, persistentContext);

          // Get the CSM object and service implementation selector
          final CollaborativeStateMachineClass csmObject = createCollaborativeStateMachine();
          final ServiceImplementationSelector serviceImplementationSelector = createServiceImplementationSelector();

          // Instantiate state machines
          // TODO: Switch to online runtime
          List<StateMachineId> instanceIds = runtime.newInstance(csmObject, serviceImplementationSelector);
          logger.info("Instantiated {} state machines.", instanceIds.size());

          runtime.waitForCompletion(5000);

          logger.info("All state machines terminated. Exiting...");
          System.exit(0);
        }
      }
    } catch (InterruptedException e) {
      logger.info("Interrupted.");
      throw CirrinaException.from(e.getMessage());
    } catch (Exception e) {
      throw CirrinaException.from("Failed to run shared runtime: %s", e);
    }
  }

  /**
   * Reads the provided CSM description file, parses the CSM description and builds a CSM object.
   *
   * @return Built collaborative state machine.
   * @throws Exception In case the collaborative state machine could not be read, parsed or built.
   */
  private CollaborativeStateMachineClass createCollaborativeStateMachine() throws Exception {
    // Read the CSM description from the given file argument
    final var csmDescription = Files.readString(sharedArgs.fileCsmDescription.toPath());
    logger.info("Successfully read CSM description.");

    // Parse the CSM description
    final var csmParser = new DescriptionParser<CollaborativeStateMachineDescription>(CollaborativeStateMachineDescription.class);
    final CollaborativeStateMachineDescription csmClass = csmParser.parse(csmDescription);
    logger.info("Parsed CSM description. Found {} state machines.", csmClass.stateMachines.size());

    // Check the CSM class
    final var csmObject = CollaborativeStateMachineClassBuilder.from(csmClass).build();
    logger.info("CSM built. Built {} state machines.", csmObject.vertexSet().size());

    return csmObject;
  }

  /**
   * Reads the provided service implementations description file, parses the service implementations description and builds the service
   * implementations.
   *
   * @return Built service implementations in a ServiceImplementationSelector
   * @throws Exception In case the service implementation descriptions could not be read, parsed or built.
   */
  private ServiceImplementationSelector createServiceImplementationSelector() throws Exception {
    // Read the service implementations description from the given file argument
    final var serviceImplementationsDescription = Files.readString(sharedArgs.fileServiceImplementations.toPath());
    logger.info("Successfully read service implementations description.");

    // Parse service implementations
    final var serviceParser = new DescriptionParser<>(ServiceImplementationsDescription.class);
    final var serviceImplementationsClass = serviceParser.parse(serviceImplementationsDescription);
    logger.info("Parsed service implementations description. Found {} service implementations.",
        serviceImplementationsClass.serviceImplementations.length);

    // Check service implementations
    final var serviceImplementations = ServicesImplementationBuilder.from(serviceImplementationsClass).build();
    logger.info("Service implementations built. Built {} service implementations.",
        serviceImplementations.size());

    return new ServiceImplementationSelector(serviceImplementations);
  }

  /**
   * The shared runtime-specific arguments.
   */
  public static class SharedArgs {

    @Parameter(
        names = {"--file-csm-description", "-fc"},
        description = "File containing the CSM description",
        required = true,
        converter = FileConverter.class
    )
    private File fileCsmDescription;

    @Parameter(
        names = {"--file-service-implementations", "-fs"},
        description = "File containing the service implementation descriptions",
        required = true,
        converter = FileConverter.class
    )
    private File fileServiceImplementations;
  }
}
