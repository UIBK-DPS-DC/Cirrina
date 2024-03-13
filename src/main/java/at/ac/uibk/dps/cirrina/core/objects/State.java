package at.ac.uibk.dps.cirrina.core.objects;

import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.actions.ActionGraph;
import at.ac.uibk.dps.cirrina.core.objects.builder.ActionGraphBuilder;
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

  public State(String name, List<Action> entryActions, List<Action> exitActions, List<Action> whileActions, boolean isAbstract,
      boolean isVirtual) {
    this.name = name;

    this.entry = ActionGraphBuilder.from(entryActions).build();
    this.exit = ActionGraphBuilder.from(exitActions).build();
    this.whilee = ActionGraphBuilder.from(whileActions).build();

    this.isAbstract = isAbstract;
    this.isVirtual = isVirtual;
  }

  public State(State parentState, List<Action> entryActions, List<Action> exitActions, List<Action> whileActions, boolean isAbstract) {
    this.name = parentState.name;

    this.entry = ActionGraphBuilder.extend(parentState.entry, entryActions).build();
    this.exit = ActionGraphBuilder.extend(parentState.exit, exitActions).build();
    this.whilee = ActionGraphBuilder.extend(parentState.whilee, whileActions).build();

    this.isAbstract = isAbstract;
    this.isVirtual = parentState.isVirtual;
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
