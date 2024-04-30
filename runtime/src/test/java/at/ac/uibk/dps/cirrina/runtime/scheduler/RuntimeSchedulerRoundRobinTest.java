package at.ac.uibk.dps.cirrina.runtime.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import at.ac.uibk.dps.cirrina.execution.command.Command;
import at.ac.uibk.dps.cirrina.execution.instance.statemachine.StateMachineInstance;
import at.ac.uibk.dps.cirrina.runtime.scheduler.RuntimeScheduler.StateMachineInstanceCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RuntimeSchedulerRoundRobinTest {

  @Test
  public void testSelectEmpty() {
    final var mockQueue = new ArrayList<StateMachineInstance>();

    final var scheduler = new RoundRobinRuntimeScheduler();

    final var selectedCommands = scheduler.select(mockQueue);

    assertEquals(0, selectedCommands.size());
  }

  @Test
  public void testSelectNoneExecutable() {
    var instanceWithoutCommand = Mockito.mock(StateMachineInstance.class);
    Mockito.when(instanceWithoutCommand.takeNextCommand()).thenReturn(Optional.empty());

    final var mockQueue = new ArrayList<StateMachineInstance>();

    mockQueue.add(instanceWithoutCommand); // 1
    mockQueue.add(instanceWithoutCommand); // 2
    mockQueue.add(instanceWithoutCommand); // 3
    mockQueue.add(instanceWithoutCommand); // 4
    mockQueue.add(instanceWithoutCommand); // 5

    final var scheduler = new RoundRobinRuntimeScheduler();

    final var selectedCommands = scheduler.select(mockQueue);

    assertEquals(0, selectedCommands.size());
  }

  @Test
  public void testSelectExecutable() {
    var mockCommand = Mockito.mock(Command.class);

    var instancesWithCommand = IntStream.range(0, 3)
        .mapToObj(i -> {
          var instanceWithCommand = Mockito.mock(StateMachineInstance.class);
          Mockito.when(instanceWithCommand.takeNextCommand()).thenReturn(Optional.of(List.of(mockCommand)));
          return instanceWithCommand;
        })
        .toArray(StateMachineInstance[]::new);

    var instanceWithoutCommand = Mockito.mock(StateMachineInstance.class);
    Mockito.when(instanceWithoutCommand.takeNextCommand()).thenReturn(Optional.empty());

    final var mockQueue = new ArrayList<StateMachineInstance>();

    mockQueue.add(instancesWithCommand[0]); // 1
    mockQueue.add(instanceWithoutCommand); // 2
    mockQueue.add(instancesWithCommand[1]); // 3
    mockQueue.add(instanceWithoutCommand); // 4
    mockQueue.add(instancesWithCommand[2]); // 5

    var scheduler = new RoundRobinRuntimeScheduler();

    var expected = IntStream.range(0, 3)
        .mapToObj(i -> List.of(new StateMachineInstanceCommand(instancesWithCommand[i], mockCommand)))
        .toList();

    final var selectedCommands = scheduler.select(mockQueue);

    assertEquals(3, selectedCommands.size());
    assertEquals(selectedCommands.get(0), scheduler.select(mockQueue).get(0));
    assertEquals(selectedCommands.get(1), scheduler.select(mockQueue).get(1));
    assertEquals(selectedCommands.get(2), scheduler.select(mockQueue).get(2));
  }
}
