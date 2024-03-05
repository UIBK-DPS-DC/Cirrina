package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.actions.ActionGraph;
import java.util.List;
import java.util.Objects;

public class ActionGraphBuilder {

  private final List<Action> actions;

  public ActionGraphBuilder(List<Action> actions) {
    this.actions = actions;
  }

  public ActionGraph build() throws IllegalArgumentException {
    Objects.requireNonNull(actions);

    // Create this action graph
    var actionGraph = new ActionGraph();

    // Build the action graph
    var it = actions.iterator();

    if (it.hasNext()) {
      var previous = new Object() {
        Action value = it.next();
      };

      // Add the first vertex
      actionGraph.addVertex(previous.value);

      it.forEachRemaining(current -> {
        // Add next vertex
        actionGraph.addVertex(current);

        // Add edge from previous to next
        actionGraph.addEdge(previous.value, current);

        // Make current previous
        previous.value = current;
      });
    }

    return actionGraph;
  }
}
