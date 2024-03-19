package at.ac.uibk.dps.cirrina.runtime.command;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.object.state.State;
import at.ac.uibk.dps.cirrina.runtime.command.statemachine.StateChangeCommand;
import at.ac.uibk.dps.cirrina.runtime.instance.StateMachineInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class StateInstanceChangeCommandTest {

  @Test
  public void TestStateChangeCommand() {
    assertDoesNotThrow(() -> {
      var stateMachineInstance = Mockito.mock(StateMachineInstance.class);

      doThrow(RuntimeException.from("")).when(stateMachineInstance).setActiveStateByName(anyString());
      doNothing().when(stateMachineInstance).setActiveStateByName("B");

      var toState = Mockito.mock(State.class);
      Mockito.when(toState.getName()).thenReturn("B");

      var command = new StateChangeCommand(stateMachineInstance, toState);
      command.execute();
    });
  }
}
