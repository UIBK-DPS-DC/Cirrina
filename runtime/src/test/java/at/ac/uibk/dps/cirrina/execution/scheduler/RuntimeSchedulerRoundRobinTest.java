package at.ac.uibk.dps.cirrina.execution.scheduler;

public class RuntimeSchedulerRoundRobinTest {

  /*@Test
  public void testSelectEmpty() {
    final var mockQueue = new ArrayList<StateMachineInstance>();

    final var scheduler = new RoundRobinRuntimeScheduler();

    final var selectedStateMachineInstance = scheduler.select(mockQueue);

    assertEquals(Optional.empty(), selectedStateMachineInstance);
  }

  @Test
  public void testSelectNoneExecutable() {
    var instanceWithoutCommand = Mockito.mock(StateMachineInstance.class);
    Mockito.when(instanceWithoutCommand.canExecute()).thenReturn(false);

    final var mockQueue = new ArrayList<StateMachineInstance>();

    mockQueue.add(instanceWithoutCommand); // 1
    mockQueue.add(instanceWithoutCommand); // 2
    mockQueue.add(instanceWithoutCommand); // 3
    mockQueue.add(instanceWithoutCommand); // 4
    mockQueue.add(instanceWithoutCommand); // 5

    final var scheduler = new RoundRobinRuntimeScheduler();

    final var selectedStateMachineInstance = scheduler.select(mockQueue);

    assertEquals(Optional.empty(), selectedStateMachineInstance);
  }

  @Test
  public void testSelectExecutable() {
    var mockCommand = Mockito.mock(ActionCommand.class);

    var instancesWithCommand = IntStream.range(0, 3)
        .mapToObj(i -> {
          var instanceWithCommand = Mockito.mock(StateMachineInstance.class);
          Mockito.when(instanceWithCommand.canExecute()).thenReturn(true);
          return instanceWithCommand;
        })
        .toArray(StateMachineInstance[]::new);

    var instanceWithoutCommand = Mockito.mock(StateMachineInstance.class);
    Mockito.when(instanceWithoutCommand.canExecute()).thenReturn(false);

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

    assertEquals(Optional.of(instancesWithCommand[0]), scheduler.select(mockQueue));
    assertEquals(Optional.of(instancesWithCommand[1]), scheduler.select(mockQueue));
    assertEquals(Optional.of(instancesWithCommand[2]), scheduler.select(mockQueue));
  }*/
}
