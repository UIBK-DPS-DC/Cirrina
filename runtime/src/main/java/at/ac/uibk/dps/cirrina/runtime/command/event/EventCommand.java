package at.ac.uibk.dps.cirrina.runtime.command.event;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.command.transition.TransitionCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.TransitionInstance;
import java.util.ArrayList;
import java.util.List;

public class EventCommand implements Command {

  private final Scope scope;

  private final Event event;

  public EventCommand(Scope scope, Event event) {
    this.scope = scope;
    this.event = event;
  }

  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    final var commands = new ArrayList<Command>();

    final var stateMachineInstance = executionContext.stateMachineInstance();
    final var stateMachine = stateMachineInstance.getStateMachine();
    final var status = stateMachineInstance.getStatus();

    // Look up the transitions that are outwards from the current state
    final var transitions = stateMachine.findOnTransitionsFromStateByEventName(status.getActivateState().getState(), event.getName());

    boolean tookTransition = false;

    for (var transition : transitions) {
      if (transition.evaluate(scope.getExtent())) {
        if (tookTransition) {
          throw RuntimeException.from("Non-determinism detected!");
        }

        commands.add(
            new TransitionCommand(
                new TransitionInstance(transition),
                stateMachineInstance.findStateInstanceByName(transition.getTarget())
                    .orElseThrow(() -> RuntimeException.from("Target state cannot be found in state machine"))
            )
        );

        tookTransition = true;
      }
    }

    return commands;
  }
}
