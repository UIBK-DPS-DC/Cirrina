package at.ac.uibk.dps.cirrina.object.state;

import at.ac.uibk.dps.cirrina.object.action.ActionGraph;
import at.ac.uibk.dps.cirrina.object.action.ActionGraphBuilder;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class State {

  public final String name;

  public final boolean isInitial;

  public final boolean isTerminal;
  public final boolean isAbstract;
  public final boolean isVirtual;
  public final ActionGraph entry;
  public final ActionGraph exit;
  public final ActionGraph whilee;

  State(StateParameters parameters) {
    if (parameters.baseState().isEmpty()) {
      this.name = parameters.name();

      this.isInitial = parameters.isInitial();
      this.isTerminal = parameters.isTerminal();

      this.entry = ActionGraphBuilder.from(parameters.entryActions()).build();
      this.exit = ActionGraphBuilder.from(parameters.exitActions()).build();
      this.whilee = ActionGraphBuilder.from(parameters.whileActions()).build();

      this.isAbstract = parameters.isAbstract();
      this.isVirtual = parameters.isVirtual();
    } else {
      var baseState = parameters.baseState().get();

      this.name = baseState.name;

      // TODO: Felix, fix for inheritance
      this.isInitial = parameters.isInitial();
      this.isTerminal = parameters.isTerminal();

      this.entry = ActionGraphBuilder.extend(new ActionGraph(baseState.entry), parameters.entryActions()).build();
      this.exit = ActionGraphBuilder.extend(new ActionGraph(baseState.exit), parameters.exitActions()).build();
      this.whilee = ActionGraphBuilder.extend(new ActionGraph(baseState.whilee), parameters.whileActions()).build();

      this.isAbstract = parameters.isAbstract();

      // Ensure overridden abstract states are virtual if they are no longer abstract, so they can be further overridden
      this.isVirtual = (baseState.isAbstract && !isAbstract) || baseState.isVirtual;
    }
  }

  public <T> List<T> getActionsOfType(Class<T> type) {
    return Stream.of(entry, exit, whilee)
        .map(actionGraph -> actionGraph.getActionsOfType(type))
        .flatMap(Collection::stream)
        .toList();
  }

  @Override
  public String toString() {
    return name;
  }
}
