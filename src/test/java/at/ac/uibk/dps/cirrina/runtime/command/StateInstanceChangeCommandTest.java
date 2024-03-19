package at.ac.uibk.dps.cirrina.runtime.command;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.runtime.command.statemachine.StateChangeCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateInstance;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class StateInstanceChangeCommandTest {

  @Test
  public void TestStateChangeCommand() {
    assertDoesNotThrow(() -> {
      var stateOne = Mockito.mock(StateInstance.class);
      var stateTwo = Mockito.mock(StateInstance.class);

      var stateMachine = Mockito.mock(StateMachineInstance.class);

      doThrow(RuntimeException.from("")).when(stateMachine).setActiveState(any());
      doNothing().when(stateMachine).setActiveState(stateOne);

      assertDoesNotThrow(() -> {
        var command = new StateChangeCommand(stateMachine, stateOne);
        command.execute();
      });

      assertThrows(RuntimeException.class, () -> {
        var command = new StateChangeCommand(stateMachine, stateTwo);
        command.execute();
      });
    });
  }
}
