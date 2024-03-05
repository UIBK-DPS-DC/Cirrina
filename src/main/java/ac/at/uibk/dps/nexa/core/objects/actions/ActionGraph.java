package ac.at.uibk.dps.nexa.core.objects.actions;

import java.util.List;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public class ActionGraph extends SimpleDirectedGraph<Action, DefaultEdge> {

  public ActionGraph() {
    super(DefaultEdge.class);
  }

  public <T> List<T> getActionsOfType(Class<T> type) {
    return vertexSet().stream()
        .filter(action -> type.isInstance(action))
        .map(type::cast)
        .toList();
  }
}
