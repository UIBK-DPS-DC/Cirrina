package at.ac.uibk.dps.cirrina.runtime.command.event;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.transition.TransitionCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.TransitionInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventCommand implements Command {

  /**
   * The current scope.
   */
  private final Scope scope;

  /**
   * The event to process.
   */
  private final Event event;

  /**
   * Initializes this always transitions command.
   *
   * @param scope The current scope.
   * @param event The event to process.
   */
  public EventCommand(Scope scope, Event event) {
    this.scope = scope;
    this.event = event;
  }

  /**
   * Executes the event command. Gets all outgoing event-based transitions for the given event and current state, resolves guards and 'else'
   * transitions and produces a transition command in case of a valid transition.
   *
   * @param executionContext Execution context.
   * @return Command list containing the transition command if a transition should be taken or an empty list.
   * @throws CirrinaException In case the command could not be executed due to non-determinism or an invalid target state.
   */
  @Override
  public List<Command> execute(ExecutionContext executionContext) throws CirrinaException {
    final var commands = new ArrayList<Command>();

    final var stateMachineInstance = executionContext.stateMachineInstance();
    final var stateMachine = stateMachineInstance.getStateMachineObject();
    final var status = stateMachineInstance.getStatus();

    // Look up the transitions that are outwards from the current state
    final var transitions = stateMachine.findOnTransitionsFromStateByEventName(status.getActivateState().getState(), event.getName());

    boolean tookTransition = false;

    for (var transition : transitions) {
      Optional<String> targetName = Optional.empty();
      boolean isElse = false;

      // Resolve the target state
      if (transition.evaluate(scope.getExtent())) {
        targetName = Optional.of(transition.getTargetName());
      } else if (transition.getElse().isPresent()) {
        targetName = transition.getElse();
        isElse = true;
      }

      // TODO: This is not correct as it ignore self-transitions
      if (targetName.isEmpty()) {
        continue;
      }

      if (tookTransition) {
        throw CirrinaException.from("Non-determinism detected!");
      }

      commands.add(
          new TransitionCommand(
              new TransitionInstance(transition),
              stateMachineInstance.findStateInstanceByName(targetName.get())
                  .orElseThrow(() -> CirrinaException.from("Target state cannot be found in state machine")),
              isElse
          )
      );

      // Set event data variables
      for (var contextVariable : event.getData()) {
        status.getActivateState().getExtent().trySet(contextVariable.name(), contextVariable.value());
      }

      tookTransition = true;
    }

    return commands;
  }
}
