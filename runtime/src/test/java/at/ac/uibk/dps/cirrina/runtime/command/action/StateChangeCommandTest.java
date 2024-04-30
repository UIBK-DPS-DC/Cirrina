package at.ac.uibk.dps.cirrina.runtime.command.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;
import at.ac.uibk.dps.cirrina.execution.command.CommandFactory;
import at.ac.uibk.dps.cirrina.execution.command.ExecutionContext;
import at.ac.uibk.dps.cirrina.execution.instance.state.StateInstance;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class StateChangeCommandTest {

  @Test
  public void testStateChangeCommand() {
    assertDoesNotThrow(() -> {
      final var stateOne = Mockito.mock(StateInstance.class);
      final var stateTwo = Mockito.mock(StateInstance.class);

      final var stateMachine = Mockito.mock(StateMachineInstance.class);

      doThrow(CirrinaException.from("")).when(stateMachine).updateActiveState(any());
      doNothing().when(stateMachine).updateActiveState(stateOne);

      final var executionContext = new ExecutionContext(stateMachine, null, null, null, null, false);

      final var commandFactory = new CommandFactory(executionContext);

      assertDoesNotThrow(() -> {
        final var command = commandFactory.createStateChangeCommand(stateOne);
        command.execute();
      });

      assertThrows(CirrinaException.class, () -> {
        final var command = commandFactory.createStateChangeCommand(stateTwo);
        command.execute();
      });
    });
  }
}
