package at.ac.uibk.dps.cirrina.core.objects.actions;

import java.util.List;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * Abstract action graph, contains actions as vertices and edges indicating the order of execution.
 * <p>
 * Formally, an action graph is a directed graph:
 * <p>
 * \( \mathcal A = (V, A) \) with:
 * <ul>
 *   <li><i>V</i> is a set of action objects;</li>
 *   <li><i>A</i> is a set of ordered pairs of action objects.</li>
 * </ul>
 * <p>
 * The order direction of actions contained within an action graph represents the order of execution
 * of the action objects contained in it.
 */
public final class ActionGraph extends SimpleDirectedGraph<Action, DefaultEdge> {

  /**
   * Initializes an empty action graph.
   */
  public ActionGraph() {
    super(DefaultEdge.class);
  }

  /**
   * Returns the actions containing in this action graph that have a certain type. The order of
   * actions is maintained.
   *
   * @param type Type of actions to return.
   * @param <T>  Type.
   * @return Actions of type.
   */
  public <T> List<T> getActionsOfType(Class<T> type) {
    return vertexSet().stream()
        .filter(action -> type.isInstance(action))
        .map(type::cast)
        .toList();
  }
}
