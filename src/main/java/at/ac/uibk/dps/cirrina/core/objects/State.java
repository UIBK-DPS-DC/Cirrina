package at.ac.uibk.dps.cirrina.core.objects;

import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.actions.ActionGraph;
import at.ac.uibk.dps.cirrina.core.objects.builder.ActionGraphBuilder;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class State {

  public final String name;

  public final boolean isAbstract;

  public final boolean isVirtual;

  private final ActionGraph entry;

  private final ActionGraph exit;

  private final ActionGraph whilee;

  public State(String name, List<Action> entryActions, List<Action> exitActions,
      List<Action> whileActions,
      boolean isAbstract, boolean isVirtual) {
    this.name = name;

    this.entry = new ActionGraphBuilder(entryActions).build();
    this.exit = new ActionGraphBuilder(exitActions).build();
    this.whilee = new ActionGraphBuilder(whileActions).build();

    this.isAbstract = isAbstract;
    this.isVirtual = isVirtual;
  }

  public State(State parentState, List<Action> entryActions, List<Action> exitActions,
      List<Action> whileActions,
      boolean isAbstract) {
    this.name = parentState.name;

    this.entry = new ActionGraphBuilder(entryActions, parentState.entry).build();
    this.exit = new ActionGraphBuilder(exitActions, parentState.exit).build();
    this.whilee = new ActionGraphBuilder(whileActions, parentState.whilee).build();

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
