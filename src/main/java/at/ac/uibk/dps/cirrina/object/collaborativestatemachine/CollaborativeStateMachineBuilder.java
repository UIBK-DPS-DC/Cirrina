package at.ac.uibk.dps.cirrina.object.collaborativestatemachine;

import at.ac.uibk.dps.cirrina.exception.VerificationException;
import at.ac.uibk.dps.cirrina.lang.classes.CollaborativeStateMachineClass;
import at.ac.uibk.dps.cirrina.lang.classes.event.EventChannel;
import at.ac.uibk.dps.cirrina.object.event.Event;
import at.ac.uibk.dps.cirrina.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.object.statemachine.StateMachineBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Collaborative state machine builder, builds a collaborative state machine based on a collaborative state machine class.
 */
public final class CollaborativeStateMachineBuilder {

  private final CollaborativeStateMachineClass collaborativeStateMachineClass;

  /**
   * Initializes a collaborative state machine builder.
   *
   * @param collaborativeStateMachineClass The collaborative state machine class used to build the collaborative state machine.
   */
  private CollaborativeStateMachineBuilder(CollaborativeStateMachineClass collaborativeStateMachineClass) {
    this.collaborativeStateMachineClass = collaborativeStateMachineClass;
  }

  public static CollaborativeStateMachineBuilder from(CollaborativeStateMachineClass collaborativeStateMachineClass) {
    return new CollaborativeStateMachineBuilder(collaborativeStateMachineClass);
  }

  /**
   * Builds the collaborative state machine graph vertices.
   *
   * @param collaborativeStateMachine The collaborative state machine being built.
   */
  private void buildVertices(CollaborativeStateMachine collaborativeStateMachine) {
    var knownStateMachines = new ArrayList<StateMachine>();

    collaborativeStateMachineClass.stateMachines.stream()
        .map(stateMachineClass -> StateMachineBuilder.from(stateMachineClass,
            knownStateMachines).build())
        .forEach(collaborativeStateMachine::addVertex);
  }

  /**
   * Builds the collaborative state machine graph edges.
   *
   * @param collaborativeStateMachine The collaborative state machine being built.
   */
  private void buildEdges(CollaborativeStateMachine collaborativeStateMachine) {
    Table<StateMachine, Event, List<StateMachine>> events = HashBasedTable.create();

    // Populate sources and targets
    populateSourceStateMachines(collaborativeStateMachine, events);
    populateTargetStateMachines(collaborativeStateMachine, events);

    // Add edges to the collaborative state machine graph
    addEdges(collaborativeStateMachine, events);
  }

  /**
   * Populates the source state machines according to pre-populated source state machines and raised events. The table is expected to
   * represent the source state machine (row), raised event (column), and target state machines (cell).
   *
   * @param collaborativeStateMachine The collaborative state machine being built.
   * @param events                    Raised events table.
   */
  private void populateSourceStateMachines(CollaborativeStateMachine collaborativeStateMachine,
      Table<StateMachine, Event, List<StateMachine>> events) {
    // Add all source state machines and their raised events
    collaborativeStateMachine.vertexSet().forEach(targetStateMachine ->
        targetStateMachine.getOutputEvents()
            .forEach(event -> events.put(targetStateMachine, event, new ArrayList<>())));
  }

  /**
   * Populates the target state machines according to pre-populated source state machines and raised events. The table is expected to
   * represent the source state machine (row), raised event (column), and target state machines (cell).
   *
   * @param collaborativeStateMachine The collaborative state machine being built.
   * @param events                    Raised events table.
   */
  private void populateTargetStateMachines(CollaborativeStateMachine collaborativeStateMachine,
      Table<StateMachine, Event, List<StateMachine>> events) {
    for (var entry : events.cellSet()) {
      var sourceStateMachine = entry.getRowKey();
      var raisedEvent = entry.getColumnKey();
      var targetStateMachines = entry.getValue();

      // Determine if the potential target state machine raises the column's raised event and append the target state
      // machine to the cell if it does
      for (var targetStateMachine : collaborativeStateMachine.vertexSet()) {
        var handledEvents = targetStateMachine.getInputEvents();

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
   * @param collaborativeStateMachine The collaborative state machine being built.
   * @param events                    Raised events table.
   */
  private void addEdges(CollaborativeStateMachine collaborativeStateMachine, Table<StateMachine, Event, List<StateMachine>> events) {
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
  public CollaborativeStateMachine build() throws VerificationException {
    try {
      var collaborativeStateMachine = new CollaborativeStateMachine(
          collaborativeStateMachineClass.name,
          collaborativeStateMachineClass.memoryMode);

      buildVertices(collaborativeStateMachine);
      buildEdges(collaborativeStateMachine);

      return collaborativeStateMachine;
    } catch (IllegalArgumentException e) {
      // We expect a checker exception which is the cause of the exception caught, in case we don't get a checker
      // exception as the cause, we don't know what to do and just rethrow. Otherwise, we throw the CheckerException
      var cause = e.getCause();
      if (!(cause instanceof VerificationException verificationException)) {
        throw e;
      }

      throw verificationException;
    }
  }
}
