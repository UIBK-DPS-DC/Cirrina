package at.ac.uibk.dps.cirrina.main;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.lang.classes.CollaborativeStateMachineClass;
import at.ac.uibk.dps.cirrina.core.lang.parser.CollaborativeStateMachineParser;
import at.ac.uibk.dps.cirrina.core.lang.parser.Parser;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine.CollaborativeStateMachineBuilder;
import at.ac.uibk.dps.cirrina.core.object.event.NatsEventHandler;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstanceId;
import at.ac.uibk.dps.cirrina.execution.service.ServiceImplementationSelector;
import at.ac.uibk.dps.cirrina.execution.service.ServicesImplementationBuilder;
import at.ac.uibk.dps.cirrina.execution.service.description.ServiceImplementationsDescription;
import at.ac.uibk.dps.cirrina.runtime.SharedRuntime;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Future;
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
    try (final var eventHandler = newEventHandler()) {
      eventHandler.subscribe(NatsEventHandler.GLOBAL_SOURCE, "*");

      try (final var persistentContext = newPersistentContext()) {
        // Create the shared runtime
        final var runtime = new SharedRuntime(eventHandler, persistentContext, getOpenTelemetry());

        // Get the CSM object and service implementation selector
        final CollaborativeStateMachine csmObject = createCollaborativeStateMachine();
        final ServiceImplementationSelector serviceImplementationSelector = createServiceImplementationSelector();

        // Instantiate state machines
        //TODO For now instantiates all state machines. Should be changed to be scripted via JEXL.
        List<StateMachineInstanceId> instanceIds = runtime.newInstance(csmObject, serviceImplementationSelector);
        logger.info("Instantiated {} state machines.", instanceIds.size());

        final List<Future<?>> stateMachineFutures = runtime.getStateMachineFutures();

        // Wait until all state machines terminated.
        for (var stateMachineFuture : stateMachineFutures) {
          stateMachineFuture.get();
        }

        logger.info("All state machines terminated. Exiting...");
        System.exit(0);
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
  private CollaborativeStateMachine createCollaborativeStateMachine() throws Exception {

    // Read the CSM description from the given file argument
    final var csmDescription = Files.readString(sharedArgs.fileCsmDescription.toPath());
    logger.info("Successfully read CSM description.");

    // Parse the CSM description
    final var csmParser = new CollaborativeStateMachineParser();
    final CollaborativeStateMachineClass csmClass = csmParser.parse(csmDescription);
    logger.info("Parsed CSM description. Found {} state machines.", csmClass.stateMachines.size());

    // Check the CSM class
    final var csmObject = CollaborativeStateMachineBuilder.from(csmClass).build();
    logger.info("CSM built. Built {} state machines.", csmObject.vertexSet().size());

    return csmObject;
  }

  /**
   * Reads the provided service implementations description file, parses the service implementations description and
   * builds the service implementations.
   *
   * @return Built service implementations in a ServiceImplementationSelector
   * @throws Exception In case the service implementation descriptions could not be read, parsed or built.
   */
  private ServiceImplementationSelector createServiceImplementationSelector() throws Exception {

    // Read the service implementations description from the given file argument
    final var serviceImplementationsDescription = Files.readString(sharedArgs.fileServiceImplementations.toPath());
    logger.info("Successfully read service implementations description.");

    // Parse service implementations
    final var serviceParser = new Parser<>(ServiceImplementationsDescription.class);
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
