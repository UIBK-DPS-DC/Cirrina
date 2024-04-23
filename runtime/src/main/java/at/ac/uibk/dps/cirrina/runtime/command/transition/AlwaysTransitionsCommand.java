package at.ac.uibk.dps.cirrina.runtime.command.transition;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import at.ac.uibk.dps.cirrina.runtime.instance.TransitionInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlwaysTransitionsCommand implements Command {

  /**
   * The current state.
   */
  private final StateInstance state;

  /**
   * Initializes this always transitions command.
   *
   * @param state The current state.
   */
  public AlwaysTransitionsCommand(StateInstance state) {
    this.state = state;
  }

  /**
   * Executes the 'always' transition command. Gets all outgoing 'always' transitions from the given state, resolves guards and 'else'
   * transitions and produces a transition command in case of a valid transition.
   *
   * @param executionContext Execution context.
   * @return Command list containing the transition command if a transition should be taken or an empty list.
   * @throws RuntimeException In case the command could not be executed due to non-determinism or an invalid target state.
   */
  @Override
  public List<Command> execute(ExecutionContext executionContext) throws RuntimeException {
    // New commands
    final var commands = new ArrayList<Command>();

    final var stateMachineInstance = executionContext.stateMachineInstance();
    final var stateMachine = stateMachineInstance.getStateMachine();
    final var status = stateMachineInstance.getStatus();

    // Look up the always-transitions that are outwards from the current state
    final var transitions = stateMachine.findAlwaysTransitionsFromState(state.getState());

    boolean tookTransition = false;

    for (var transition : transitions) {

      Optional<String> targetName = Optional.empty();
      boolean isElse = false;

      // Resolve the target state
      if (transition.evaluate(status.getActivateState().getExtent())) {
        targetName = Optional.of(transition.getTargetName());
      } else if (transition.getElse().isPresent()) {
        targetName = transition.getElse();
        isElse = true;
      }

      if (targetName.isEmpty()) {
        continue;
      }

      if (tookTransition) {
        throw RuntimeException.from("Non-determinism detected!");
      }

      commands.add(
          new TransitionCommand(
              new TransitionInstance(transition),
              stateMachineInstance.findStateInstanceByName(targetName.get())
                  .orElseThrow(() -> RuntimeException.from("Target state cannot be found in state machine")),
              isElse
          )
      );

      tookTransition = true;
    }

    return commands;
  }
}
