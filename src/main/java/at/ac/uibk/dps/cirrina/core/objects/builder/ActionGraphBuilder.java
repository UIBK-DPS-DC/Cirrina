package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.actions.ActionGraph;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Objects;

public final class ActionGraphBuilder {

  private final List<Action> actions;

  private final ActionGraph actionGraph;

  public ActionGraphBuilder(List<Action> actions) {
    this(actions, new ActionGraph());
  }

  public ActionGraphBuilder(List<Action> actions, ActionGraph actionGraph) {
    this.actions = actions;
    this.actionGraph = actionGraph;
  }

  public ActionGraph build() throws IllegalArgumentException {
    Objects.requireNonNull(actions);

    // Build the action graph
    var it = actions.iterator();

    if (it.hasNext()) {

      Action previous;

      // If the action graph is not empty, connect to its last vertex
      if (!actionGraph.vertexSet().isEmpty()) {
        previous = Iterables.getLast(actionGraph.vertexSet());
      } else {
        previous = it.next();

        // Add the first vertex
        actionGraph.addVertex(previous);
      }

      while (it.hasNext()) {
        Action current = it.next();

        // Add next vertex
        actionGraph.addVertex(current);

        // Add edge from previous to next
        actionGraph.addEdge(previous, current);

        // Make current previous
        previous = current;
      }
    }

    return actionGraph;
  }
}
