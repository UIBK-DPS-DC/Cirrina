package at.ac.uibk.dps.cirrina.core.object.transition;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.io.plantuml.Exportable;
import at.ac.uibk.dps.cirrina.core.io.plantuml.PlantUmlVisitor;
import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.ActionGraph;
import at.ac.uibk.dps.cirrina.core.object.action.ActionGraphBuilder;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import at.ac.uibk.dps.cirrina.core.object.guard.Guard;
import at.ac.uibk.dps.cirrina.core.object.state.State;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jgrapht.graph.DefaultEdge;

/**
 * Transition, represents a transition that can be 'taken' between two states in a state machine. The base transition is not event-triggered
 * meaning that it is 'taken' whenever the guards all produce a true value. Actions can be executed during a transition. The else property
 * of the transition represents the target state whenever the transition is not taken (at least one of the guards do not produce a true
 * value).
 */
public class Transition extends DefaultEdge implements Exportable {

  private final String targetName;
  private final List<Guard> guards;
  private final Optional<String> elseTargetName;
  private final ActionGraph actionGraph;

  /**
   * Initializes this transition object.
   *
   * @param targetName Name of the target state.
   * @param elseTargetName      Name of the else state.
   * @param guards     List of guards.
   * @param actions    List of actions.
   */
  Transition(String targetName, Optional<String> elseTargetName, List<Guard> guards, List<Action> actions) {
    this.targetName = targetName;
    this.guards = guards;
    this.elseTargetName = elseTargetName;

    this.actionGraph = ActionGraphBuilder.from(actions).build();
  }

  /**
   * Evaluate all guards of this transition. A return value of true indicates that all guard conditions produced true, a return value of
   * false indicates that at least one guard condition returned false.
   *
   * @param extent Extent describing variables in scope.
   * @return True if the transition can be taken based on the guards, otherwise false.
   * @throws RuntimeException If some guard expression could not be evaluated, or some guard expression does not produce a boolean value.
   */
  public boolean evaluate(Extent extent) throws RuntimeException {
    for (var guard : guards) {
      if (!guard.evaluate(extent)) {
        return false;
      }
    }

    return true;
  }


  /**
   * Returns the guards.
   *
   * @return Guards.
   */
  public List<Guard> getGuards() {
    return guards;
  }

  /**
   * Returns the action graph.
   *
   * @return Action graph.
   */
  public ActionGraph getActionGraph() {
    return actionGraph;
  }

  /**
   * Returns the name of the target state.
   *
   * @return Name of the target state.
   */
  @Override
  public State getSource() {
    return (State) super.getSource();
  }

  /**
   * Returns the name of the target state.
   *
   * @return Name of the target state.
   */
  @Override
  public State getTarget() {
    return (State) super.getTarget();
  }

  /**
   * Returns the name of the target state.
   *
   * @return Name of the target state.
   */
  public String getTargetName() {
    return targetName;
  }

  /**
   * Returns the name of the else state.
   *
   * @return Name of the else state.
   */
  public Optional<String> getElse() {
    return elseTargetName;
  }

  /**
   * Returns actions contained in the action graph by type.
   *
   * @param type Type to return.
   * @param <T>  Type to return.
   * @return Actions of type.
   */
  public <T> List<T> getActionsOfType(Class<T> type) {
    return Stream.of(actionGraph)
        .map(actionGraph -> actionGraph.getActionsOfType(type))
        .flatMap(s -> s.stream())
        .toList();
  }

  @Override
  public void accept(PlantUmlVisitor visitor) {
    visitor.visit(this);
  }
}
