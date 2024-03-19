package at.ac.uibk.dps.cirrina.runtime.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import at.ac.uibk.dps.cirrina.runtime.StateMachineInstance;
import at.ac.uibk.dps.cirrina.runtime.command.Command;
import at.ac.uibk.dps.cirrina.runtime.scheduler.Scheduler.StateMachineInstanceCommand;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SchedulerRoundRobinTest {

  @Test
  public void testSelectEmpty() {
    Queue<StateMachineInstance> mockQueue = new LinkedList<>();

    var scheduler = new RoundRobinScheduler();

    assertEquals(Optional.empty(), scheduler.select(mockQueue));
  }

  @Test
  public void testSelectNoneExecutable() {
    var instanceWithoutCommand = Mockito.mock(StateMachineInstance.class);
    Mockito.when(instanceWithoutCommand.getExecutableCommand()).thenReturn(Optional.empty());

    Queue<StateMachineInstance> mockQueue = new LinkedList<>();

    mockQueue.add(instanceWithoutCommand); // 1
    mockQueue.add(instanceWithoutCommand); // 2
    mockQueue.add(instanceWithoutCommand); // 3
    mockQueue.add(instanceWithoutCommand); // 4
    mockQueue.add(instanceWithoutCommand); // 5

    var scheduler = new RoundRobinScheduler();

    assertEquals(Optional.empty(), scheduler.select(mockQueue));
  }

  @Test
  public void testSelectExecutable() {
    var mockCommand = Mockito.mock(Command.class);

    var instancesWithCommand = IntStream.range(0, 3)
        .mapToObj(i -> {
          var instanceWithCommand = Mockito.mock(StateMachineInstance.class);
          Mockito.when(instanceWithCommand.getExecutableCommand()).thenReturn(Optional.of(mockCommand));
          return instanceWithCommand;
        })
        .toArray(StateMachineInstance[]::new);

    var instanceWithoutCommand = Mockito.mock(StateMachineInstance.class);
    Mockito.when(instanceWithoutCommand.getExecutableCommand()).thenReturn(Optional.empty());

    Queue<StateMachineInstance> mockQueue = new LinkedList<>();

    mockQueue.add(instancesWithCommand[0]); // 1
    mockQueue.add(instanceWithoutCommand); // 2
    mockQueue.add(instancesWithCommand[1]); // 3
    mockQueue.add(instanceWithoutCommand); // 4
    mockQueue.add(instancesWithCommand[2]); // 5

    var scheduler = new RoundRobinScheduler();

    var expected = IntStream.range(0, 3)
        .mapToObj(i -> {
          return Optional.of(new StateMachineInstanceCommand(instancesWithCommand[i], mockCommand));
        })
        .toArray(Optional[]::new);

    assertEquals(expected[0], scheduler.select(mockQueue));
    assertEquals(expected[1], scheduler.select(mockQueue));
    assertEquals(expected[2], scheduler.select(mockQueue));
    assertEquals(expected[0], scheduler.select(mockQueue));
    assertEquals(expected[1], scheduler.select(mockQueue));
    assertEquals(expected[2], scheduler.select(mockQueue));
  }
}
