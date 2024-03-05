package ac.at.uibk.dps.nexa.core.objects.builder;

import ac.at.uibk.dps.nexa.core.Event;
import ac.at.uibk.dps.nexa.core.objects.CollaborativeStateMachine;
import ac.at.uibk.dps.nexa.core.objects.StateMachine;
import ac.at.uibk.dps.nexa.lang.parser.classes.CollaborativeStateMachineClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.EventChannel;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.List;

public class CollaborativeStateMachineBuilder {

  private final CollaborativeStateMachineClass collaborativeStateMachineClass;

  private final CollaborativeStateMachine collaborativeStateMachine;

  public CollaborativeStateMachineBuilder(CollaborativeStateMachineClass collaborativeStateMachineClass) {
    this.collaborativeStateMachineClass = collaborativeStateMachineClass;

    this.collaborativeStateMachine = new CollaborativeStateMachine(collaborativeStateMachineClass.name,
        collaborativeStateMachineClass.memoryMode);
  }

  private void buildVertices() {
    collaborativeStateMachineClass.stateMachines.stream()
        .map(stateMachineClass -> new StateMachineBuilder(stateMachineClass).build())
        .forEach(collaborativeStateMachine::addVertex);
  }

  private void buildEdges() {
    Table<StateMachine, Event, List<StateMachine>> events = HashBasedTable.create();

    // Populate sources and targets
    populateSourceStateMachines(events);
    populateTargetStateMachines(events);

    addEdges(events);
  }

  private void populateSourceStateMachines(Table<StateMachine, Event, List<StateMachine>> events) {
    collaborativeStateMachine.vertexSet().forEach(targetStateMachine ->
        targetStateMachine.getRaisedEvents()
            .forEach(event -> events.put(targetStateMachine, event, new ArrayList<>())));
  }

  private void populateTargetStateMachines(Table<StateMachine, Event, List<StateMachine>> events) {
    for (var entry : events.cellSet()) {
      var sourceStateMachine = entry.getRowKey();
      var raisedEvent = entry.getColumnKey();
      var targetStateMachines = entry.getValue();

      for (var targetStateMachine : collaborativeStateMachine.vertexSet()) {
        var handledEvents = targetStateMachine.getHandledEvents();

        if ((raisedEvent.channel == EventChannel.INTERNAL && sourceStateMachine == targetStateMachine
            || raisedEvent.channel == EventChannel.GLOBAL
            || raisedEvent.channel == EventChannel.EXTERNAL)
            && handledEvents.contains(raisedEvent.name)) {
          targetStateMachines.add(targetStateMachine);
        }
      }
    }
  }

  private void addEdges(Table<StateMachine, Event, List<StateMachine>> events) {

  }

  public CollaborativeStateMachine build()
      throws IllegalArgumentException {
    buildVertices();
    buildEdges();

    return collaborativeStateMachine;
  }
}
