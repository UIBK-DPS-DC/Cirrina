package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.objects.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.objects.Event;
import at.ac.uibk.dps.cirrina.core.objects.StateMachine;
import at.ac.uibk.dps.cirrina.lang.parser.classes.CollaborativeStateMachineClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.events.EventChannel;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Collaborative state machine builder, builds a collaborative state machine based on a collaborative state machine class.
 */
public final class CollaborativeStateMachineBuilder {

  private final CollaborativeStateMachineClass collaborativeStateMachineClass;

  private final CollaborativeStateMachine collaborativeStateMachine;

  /**
   * Initializes a collaborative state machine builder.
   *
   * @param collaborativeStateMachineClass The collaborative state machine class used to build the collaborative state machine.
   */
  private CollaborativeStateMachineBuilder(CollaborativeStateMachineClass collaborativeStateMachineClass) {
    this.collaborativeStateMachineClass = collaborativeStateMachineClass;

    this.collaborativeStateMachine = new CollaborativeStateMachine(
        collaborativeStateMachineClass.name,
        collaborativeStateMachineClass.memoryMode);
  }

  public static CollaborativeStateMachineBuilder from(CollaborativeStateMachineClass collaborativeStateMachineClass) {
    return new CollaborativeStateMachineBuilder(collaborativeStateMachineClass);
  }

  /**
   * Builds the collaborative state machine graph vertices.
   */
  private void buildVertices() {
    var knownStateMachines = new ArrayList<StateMachine>();

    collaborativeStateMachineClass.stateMachines.stream()
        .map(stateMachineClass -> StateMachineBuilder.from(stateMachineClass,
            knownStateMachines).build())
        .forEach(collaborativeStateMachine::addVertex);
  }

  /**
   * Builds the collaborative state machine graph edges.
   */
  private void buildEdges() {
    Table<StateMachine, Event, List<StateMachine>> events = HashBasedTable.create();

    // Populate sources and targets
    populateSourceStateMachines(events);
    populateTargetStateMachines(events);

    // Add edges to the collaborative state machine graph
    addEdges(events);
  }

  /**
   * Populates the source state machines according to pre-populated source state machines and raised events. The table is expected to
   * represent the source state machine (row), raised event (column), and target state machines (cell).
   *
   * @param events Raised events table.
   */
  private void populateSourceStateMachines(Table<StateMachine, Event, List<StateMachine>> events) {
    // Add all source state machines and their raised events
    collaborativeStateMachine.vertexSet().forEach(targetStateMachine ->
        targetStateMachine.getRaisedEvents()
            .forEach(event -> events.put(targetStateMachine, event, new ArrayList<>())));
  }

  /**
   * Populates the target state machines according to pre-populated source state machines and raised events. The table is expected to
   * represent the source state machine (row), raised event (column), and target state machines (cell).
   *
   * @param events Raised events table.
   */
  private void populateTargetStateMachines(Table<StateMachine, Event, List<StateMachine>> events) {
    for (var entry : events.cellSet()) {
      var sourceStateMachine = entry.getRowKey();
      var raisedEvent = entry.getColumnKey();
      var targetStateMachines = entry.getValue();

      // Determine if the potential target state machine raises the column's raised event and append the target state
      // machine to the cell if it does
      for (var targetStateMachine : collaborativeStateMachine.vertexSet()) {
        var handledEvents = targetStateMachine.getHandledEvents();

        // If the raised event is internal, the source- and target state machines need to match, otherwise it can be
        // added to the table. At this point we assume that external events will be bound, we cannot resolve the case
        // that it does not here
        if ((
            raisedEvent.channel == EventChannel.INTERNAL && sourceStateMachine == targetStateMachine
                || raisedEvent.channel == EventChannel.GLOBAL
                || raisedEvent.channel == EventChannel.EXTERNAL)
            && handledEvents.contains(raisedEvent.name)) {
          targetStateMachines.add(targetStateMachine);
        }
      }
    }
  }

  /**
   * Add edges to the currently building collaborative state machine according to the provided table. The table is expected to represent the
   * source state machine (row), raised event (column), and target state machines (cell).
   *
   * @param events Raised events table.
   */
  private void addEdges(Table<StateMachine, Event, List<StateMachine>> events) {
    // Process the events table and add all edges
    for (var rowEntry : events.rowMap().entrySet()) {
      var sourceStateMachine = rowEntry.getKey();

      for (var colEntry : rowEntry.getValue().entrySet()) {
        var raisedEvent = colEntry.getKey();

        for (var targetStateMachine : colEntry.getValue()) {
          collaborativeStateMachine.addEdge(sourceStateMachine, targetStateMachine, raisedEvent);
        }
      }
    }
  }

  /**
   * Builds the collaborative state machine.
   *
   * @return Built collaborative state machine.
   * @throws IllegalArgumentException In case the collaborative state machine could not be built.
   */
  public CollaborativeStateMachine build() throws IllegalArgumentException {
    buildVertices();
    buildEdges();

    return collaborativeStateMachine;
  }
}
