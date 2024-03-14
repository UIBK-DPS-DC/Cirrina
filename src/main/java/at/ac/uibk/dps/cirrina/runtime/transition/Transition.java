package at.ac.uibk.dps.cirrina.runtime.transition;

import at.ac.uibk.dps.cirrina.runtime.action.Action;
import at.ac.uibk.dps.cirrina.runtime.action.ActionGraph;
import at.ac.uibk.dps.cirrina.runtime.action.ActionGraphBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jgrapht.graph.DefaultEdge;

public class Transition extends DefaultEdge {

  public final String target;
  public final Optional<String> elsee;

  public final ActionGraph actions;

  Transition(String target, Optional<String> elsee, List<Action> actions) {
    this.target = target;
    this.elsee = elsee;

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
