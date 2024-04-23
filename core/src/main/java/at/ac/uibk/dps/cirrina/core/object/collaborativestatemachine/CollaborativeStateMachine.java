package at.ac.uibk.dps.cirrina.core.object.collaborativestatemachine;

import at.ac.uibk.dps.cirrina.core.object.event.Event;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import java.util.List;
import java.util.Optional;
import org.jgrapht.graph.DirectedPseudograph;

public final class CollaborativeStateMachine extends DirectedPseudograph<StateMachine, Event> {

  private final String name;

  CollaborativeStateMachine(String name) {
    super(Event.class);

    this.name = name;
  }

  /**
   * Returns a state machine by its name. If no state machine is known with the supplied name, empty is returned.
   *
   * @param name Name of the state machine to return.
   * @return The state machine with the supplied name or empty.
   */
  public Optional<StateMachine> findStateMachineByName(String name) {
    // Attempt to match the provided name to a known state machine
    var states = vertexSet().stream()
        .filter(state -> state.getName().equals(name))
        .toList();

    // Expect precisely one state machine with the provided name
    if (states.size() != 1) {
      return Optional.empty();
    }
    return Optional.of(states.getFirst());
  }
  
  /**
   * Returns the collection of state machines.
   *
   * @return State machines.
   */
  public List<StateMachine> getStateMachines() {
    return List.copyOf(vertexSet());
  }

  @Override
  public String toString() {
    return name;
  }
}
