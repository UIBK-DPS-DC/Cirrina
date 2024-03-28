package at.ac.uibk.dps.cirrina.core.runtime.command.action;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.runtime.command.Command.ExecutionContext;
import at.ac.uibk.dps.cirrina.core.runtime.command.statemachine.StateChangeCommand;
import at.ac.uibk.dps.cirrina.core.runtime.instance.StateInstance;
import at.ac.uibk.dps.cirrina.core.runtime.instance.StateMachineInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class StateChangeCommandTest {

  @Test
  public void testStateChangeCommand() {
    assertDoesNotThrow(() -> {
      var stateOne = Mockito.mock(StateInstance.class);
      var stateTwo = Mockito.mock(StateInstance.class);

      var stateMachine = Mockito.mock(StateMachineInstance.class);

      doThrow(RuntimeException.from("")).when(stateMachine).setActiveState(any());
      doNothing().when(stateMachine).setActiveState(stateOne);

      assertDoesNotThrow(() -> {
        var command = new StateChangeCommand(stateOne);
        command.execute(new ExecutionContext(stateMachine, null));
      });

      assertThrows(RuntimeException.class, () -> {
        var command = new StateChangeCommand(stateTwo);
        command.execute(new ExecutionContext(stateMachine, null));
      });
    });
  }
}
