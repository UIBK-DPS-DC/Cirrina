package at.ac.uibk.dps.cirrina.core.object.transition;

import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.ActionGraph;
import at.ac.uibk.dps.cirrina.core.object.builder.ActionGraphBuilder;
import java.util.List;
import java.util.stream.Stream;
import org.jgrapht.graph.DefaultEdge;

public class Transition extends DefaultEdge {

  public final String target;

  public final ActionGraph actions;

  public Transition(String target, List<Action> actions) {
    this.target = target;

    this.actions = ActionGraphBuilder.from(actions).build();
  }

  public <T> List<T> getActionsOfType(Class<T> type) {
    return Stream.of(actions)
        .map(actionGraph -> actionGraph.getActionsOfType(type))
        .flatMap(s -> s.stream())
        .toList();
  }

  public List<Action> allActions() {
    return actions.vertexSet().stream().toList();
  }
}
