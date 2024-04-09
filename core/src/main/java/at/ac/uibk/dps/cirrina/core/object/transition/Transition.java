package at.ac.uibk.dps.cirrina.core.object.transition;

import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.ActionGraph;
import at.ac.uibk.dps.cirrina.core.object.action.ActionGraphBuilder;
import at.ac.uibk.dps.cirrina.core.object.guard.Guard;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jgrapht.graph.DefaultEdge;

public class Transition extends DefaultEdge {

  private final String target;
  private final List<Guard> guards;
  private final Optional<String> elsee;
  private final ActionGraph actionGraph;

  Transition(String target, Optional<String> elsee, List<Guard> guards, List<Action> actions) {
    this.target = target;
    this.guards = guards;
    this.elsee = elsee;

    this.actionGraph = ActionGraphBuilder.from(actions).build();
  }

  public List<Guard> getGuards() {
    return guards;
  }

  public ActionGraph getActionGraph() {
    return actionGraph;
  }

  @Override
  public String getTarget() {
    return target;
  }

  public Optional<String> getElse() {
    return elsee;
  }

  public <T> List<T> getActionsOfType(Class<T> type) {
    return Stream.of(actionGraph)
        .map(actionGraph -> actionGraph.getActionsOfType(type))
        .flatMap(s -> s.stream())
        .toList();
  }
}
