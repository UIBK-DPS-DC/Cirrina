package at.ac.uibk.dps.cirrina.core.object.state;

import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.ActionGraph;
import at.ac.uibk.dps.cirrina.core.object.action.ActionGraphBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class State {

  private final String name;
  private final boolean isInitial;
  private final boolean isTerminal;
  private final boolean isAbstract;
  private final boolean isVirtual;
  private final ActionGraph entryActionGraph;
  private final ActionGraph exitActionGraph;
  private final ActionGraph whileActionGraph;

  State(Parameters parameters) {
    if (parameters.baseState().isEmpty()) {
      this.name = parameters.name();

      this.isInitial = parameters.isInitial();
      this.isTerminal = parameters.isTerminal();

      this.entryActionGraph = ActionGraphBuilder.from(parameters.entryActions()).build();
      this.exitActionGraph = ActionGraphBuilder.from(parameters.exitActions()).build();
      this.whileActionGraph = ActionGraphBuilder.from(parameters.whileActions()).build();

      this.isAbstract = parameters.isAbstract();
      this.isVirtual = parameters.isVirtual();
    } else {
      var baseState = parameters.baseState().get();

      this.name = baseState.name;

      this.isInitial = parameters.isInitial() || baseState.isInitial;
      this.isTerminal = parameters.isTerminal() || baseState.isTerminal;

      this.entryActionGraph = ActionGraphBuilder.extend(new ActionGraph(baseState.entryActionGraph), parameters.entryActions()).build();
      this.exitActionGraph = ActionGraphBuilder.extend(new ActionGraph(baseState.exitActionGraph), parameters.exitActions()).build();
      this.whileActionGraph = ActionGraphBuilder.extend(new ActionGraph(baseState.whileActionGraph), parameters.whileActions()).build();

      this.isAbstract = parameters.isAbstract();

      // Ensure overridden abstract states are virtual if they are no longer abstract, so they can be further overridden
      this.isVirtual = (baseState.isAbstract && !isAbstract) || baseState.isVirtual;
    }
  }

  public String getName() {
    return name;
  }

  public boolean isInitial() {
    return isInitial;
  }

  public boolean isTerminal() {
    return isTerminal;
  }

  public boolean isAbstract() {
    return isAbstract;
  }

  public boolean isVirtual() {
    return isVirtual;
  }

  public ActionGraph getEntryActionGraph() {
    return entryActionGraph;
  }

  public ActionGraph getExitActionGraph() {
    return exitActionGraph;
  }

  public ActionGraph getWhileActionGraph() {
    return whileActionGraph;
  }

  public <T> List<T> getActionsOfType(Class<T> type) {
    return Stream.of(entryActionGraph, exitActionGraph, whileActionGraph)
        .map(actionGraph -> actionGraph.getActionsOfType(type))
        .flatMap(Collection::stream)
        .toList();
  }

  @Override
  public String toString() {
    return name;
  }

  record Parameters(
      String name,
      boolean isInitial,
      boolean isTerminal,
      List<Action> entryActions,
      List<Action> exitActions,
      List<Action> whileActions,
      boolean isAbstract,
      boolean isVirtual,
      Optional<State> baseState
  ) {

  }
}
