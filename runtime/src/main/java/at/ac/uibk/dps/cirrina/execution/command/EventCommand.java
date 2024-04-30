package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import at.ac.uibk.dps.cirrina.execution.instance.transition.TransitionInstance;
import java.util.ArrayList;
import java.util.Optional;

public class EventCommand extends Command {

  private final Event event;

  EventCommand(ExecutionContext executionContext, Event event) {
    super(executionContext);

    this.event = event;
  }

  @Override
  public void execute() throws CirrinaException {
    // Require state machine scope
    final var stateMachineInstance = (StateMachineInstance) executionContext.scope();

    if (stateMachineInstance == null) {
      throw CirrinaException.from("Event scope must be a state machine instance");
    }

    // New commands
    final var commands = new ArrayList<Command>();
    final var commandFactory = new CommandFactory(executionContext);

    final var extent = executionContext.scope().getExtent();
    final var status = executionContext.status();
    final var stateMachineObject = stateMachineInstance.getStateMachineObject();

    // Look up the transitions that are outwards from the current state
    final var transitionObjects = stateMachineObject
        .findOnTransitionsFromStateByEventName(status.getActivateState().getState(),
            event.getName());

    boolean tookTransition = false;

    for (var transitionObject : transitionObjects) {
      Optional<String> targetName = Optional.empty();
      boolean isElse = false;

      // Resolve the target state
      if (transitionObject.evaluate(extent)) {
        targetName = Optional.of(transitionObject.getTargetName());
      } else if (transitionObject.getElse().isPresent()) {
        targetName = transitionObject.getElse();
        isElse = true;
      }

      if (targetName.isEmpty()) {
        continue;
      }

      if (tookTransition) {
        throw CirrinaException.from("Non-determinism detected!");
      }

      // Construct a transition instance
      final var transitionInstance = new TransitionInstance(transitionObject);

      // Acquire the target state instance
      final var targetStateInstance = stateMachineInstance
          .findStateInstanceByName(targetName.get())
          .orElseThrow(() -> CirrinaException.from("Target state cannot be found in state machine"));

      // Create and add the transition command
      final var transitionCommand = commandFactory.createTransitionCommand(transitionInstance, targetStateInstance, isElse);

      commands.add(transitionCommand);

      // Set event data variables
      for (var contextVariable : event.getData()) {
        extent.trySet(contextVariable.name(), contextVariable.value());
      }

      tookTransition = true;
    }

    // Add commands to the front of the queue, replacing this executed command
    final var commandQueue = executionContext.commandQueueAdapter();

    commandQueue.addCommandsToFront(commands);
  }
}
