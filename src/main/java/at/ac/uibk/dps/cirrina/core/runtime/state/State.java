package at.ac.uibk.dps.cirrina.core.runtime.state;

import at.ac.uibk.dps.cirrina.core.runtime.action.Action;
import at.ac.uibk.dps.cirrina.core.runtime.action.ActionGraph;
import at.ac.uibk.dps.cirrina.core.runtime.action.ActionGraphBuilder;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class State {

  public final String name;

  public final boolean isAbstract;

  public final boolean isVirtual;

  private final ActionGraph entry;

  private final ActionGraph exit;

  private final ActionGraph whilee;

  State(String name, List<Action> entryActions, List<Action> exitActions, List<Action> whileActions, boolean isAbstract,
      boolean isVirtual) {
    this.name = name;

    this.entry = ActionGraphBuilder.from(entryActions).build();
    this.exit = ActionGraphBuilder.from(exitActions).build();
    this.whilee = ActionGraphBuilder.from(whileActions).build();

    this.isAbstract = isAbstract;
    this.isVirtual = isVirtual;
  }

  State(State baseState, List<Action> entryActions, List<Action> exitActions,
      List<Action> whileActions,
      boolean isAbstract) {
    this.name = baseState.name;

    this.entry = ActionGraphBuilder.extend(new ActionGraph(baseState.entry), entryActions).build();
    this.exit = ActionGraphBuilder.extend(new ActionGraph(baseState.exit), exitActions).build();
    this.whilee = ActionGraphBuilder.extend(new ActionGraph(baseState.whilee), whileActions).build();

    this.isAbstract = isAbstract;
    // Ensure overridden abstract states are virtual if they are no longer abstract, so they can be further overridden
    this.isVirtual = (baseState.isAbstract && !isAbstract) || baseState.isVirtual;
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
