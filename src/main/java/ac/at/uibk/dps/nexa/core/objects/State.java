package ac.at.uibk.dps.nexa.core.objects;

import ac.at.uibk.dps.nexa.core.objects.actions.Action;
import ac.at.uibk.dps.nexa.core.objects.actions.ActionGraph;
import ac.at.uibk.dps.nexa.core.objects.builder.ActionGraphBuilder;
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

  public State(String name, List<Action> entryActions, List<Action> exitActions, List<Action> whileActions,
      boolean isAbstract, boolean isVirtual) {
    this.name = name;

    this.entry = new ActionGraphBuilder(entryActions).build();
    this.exit = new ActionGraphBuilder(exitActions).build();
    this.whilee = new ActionGraphBuilder(whileActions).build();

    this.isAbstract = isAbstract;
    this.isVirtual = isVirtual;
  }

  public State(State state) {
    this.name = state.name;

    this.entry = state.entry;
    this.exit = state.exit;
    this.whilee = state.whilee;

    this.isAbstract = state.isAbstract;
    this.isVirtual = state.isVirtual;
  }

  public <T> List<T> getActionsOfType(Class<T> type) {
    return Stream.of(entry, exit, whilee)
        .map(actionGraph -> actionGraph.getActionsOfType(type))
        .flatMap(Collection::stream)
        .toList();
  }
}
