package at.ac.uibk.dps.cirrina.core.objects;

import at.ac.uibk.dps.cirrina.lang.parser.keywords.MemoryMode;
import java.util.Optional;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

public class CollaborativeStateMachine extends DirectedMultigraph<StateMachine, DefaultEdge> {

  public final String name;

  public final MemoryMode memoryMode;

  public CollaborativeStateMachine(String name, MemoryMode memoryMode) {
    super(DefaultEdge.class);

    this.name = name;
    this.memoryMode = memoryMode;
  }


  /**
   * Returns a state machine by its name. If no state machine is known with the supplied name, empty is returned.
   *
   * @param name Name of the state machine to return.
   * @return The state machine with the supplied name or empty.
   */
  public Optional<StateMachine> getStateMachineByName(String name) {
    // Attempt to match the provided name to a known state machine
    var states = vertexSet().stream()
        .filter(state -> state.getName().equals(name))
        .toList();

    // Expect precisely one state machine with the provided name
    if (states.size() != 1) {
      return Optional.empty();
    }
    return Optional.ofNullable(states.getFirst());
  }
}
