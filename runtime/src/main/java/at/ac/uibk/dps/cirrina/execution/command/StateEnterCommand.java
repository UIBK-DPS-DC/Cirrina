package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.execution.instance.state.StateInstance;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import at.ac.uibk.dps.cirrina.execution.instance.transition.TransitionInstance;
import java.util.ArrayList;
import java.util.Optional;
import org.jgrapht.traverse.TopologicalOrderIterator;

public final class StateEnterCommand extends Command {

  private final StateInstance enteringStateInstance;

  StateEnterCommand(ExecutionContext executionContext, StateInstance enteringStateInstance) {
    super(executionContext);

    this.enteringStateInstance = enteringStateInstance;
  }

  @Override
  public void execute() throws CirrinaException {
    // Require state machine scope
    final var stateMachineInstance = (StateMachineInstance) executionContext.scope();

    if (stateMachineInstance == null) {
      throw CirrinaException.from("Event scope must be a state machine instance");
    }

    final var enteringStateObject = enteringStateInstance.getState();

    // Create new execution context with state scope
    final var stateScopeExecutionContext = executionContext.withScope(enteringStateInstance);
    final var whileStateScopeExecutionContext = stateScopeExecutionContext.withIsWhile(true);

    // New commands
    final var commands = new ArrayList<Command>();
    final var stateMachineScopeCommandFactory = new CommandFactory(executionContext);
    final var stateScopeCommandFactory = new CommandFactory(stateScopeExecutionContext);
    final var stateScopeWhileCommandFactory = new CommandFactory(whileStateScopeExecutionContext);

    final var extent = executionContext.scope().getExtent();
    final var stateMachineObject = stateMachineInstance.getStateMachineObject();

    // Append the entry actions to the command list
    new TopologicalOrderIterator<>(enteringStateObject.getEntryActionGraph()).forEachRemaining(
        action -> commands.add(stateScopeCommandFactory.createActionCommand(action)));

    // Append the while actions to the command list
    new TopologicalOrderIterator<>(enteringStateObject.getWhileActionGraph()).forEachRemaining(
        action -> commands.add(stateScopeWhileCommandFactory.createActionCommand(action)));

    // Look up the always-transitions that are outwards from the current state
    final var transitions = stateMachineObject.findAlwaysTransitionsFromState(enteringStateObject);

    boolean tookTransition = false;

    for (var transition : transitions) {
      Optional<String> targetName = Optional.empty();

      boolean isElse = false;

      // Resolve the target state
      if (transition.evaluate(extent)) {
        targetName = Optional.of(transition.getTargetName());
      } else if (transition.getElse().isPresent()) {
        targetName = transition.getElse();
        isElse = true;
      }

      if (targetName.isEmpty()) {
        continue;
      }

      if (tookTransition) {
        throw CirrinaException.from("Non-determinism detected!");
      }

      final var transitionInstance = new TransitionInstance(transition);

      commands.add(
          stateMachineScopeCommandFactory.createTransitionCommand(
              transitionInstance,
              stateMachineInstance.findStateInstanceByName(targetName.get())
                  .orElseThrow(() -> CirrinaException.from("Target state cannot be found in state machine")),
              isElse
          )
      );

      tookTransition = true;
    }

    // Start all timeout actions
    {
      final var it = new TopologicalOrderIterator<>(enteringStateObject.getAfterActionGraph());
      while (it.hasNext()) {
        final var action = it.next();

        stateMachineInstance.startTimeoutAction((TimeoutAction) action);
      }
    }

    // Add commands to the front of the queue, replacing this executed command
    final var commandQueue = executionContext.commandQueueAdapter();

    commandQueue.addCommandsToFront(commands);
  }
}
