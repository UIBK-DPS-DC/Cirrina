package at.ac.uibk.dps.cirrina.core.object.state;

import at.ac.uibk.dps.cirrina.core.io.plantuml.Exportable;
import at.ac.uibk.dps.cirrina.core.io.plantuml.PlantUmlVisitor;
import at.ac.uibk.dps.cirrina.core.lang.classes.context.ContextClass;
import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.ActionGraph;
import at.ac.uibk.dps.cirrina.core.object.action.ActionGraphBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public final class State implements Exportable {

  private final UUID parentStateMachineId;
  private final String name;
  private final Optional<ContextClass> localContextClass;
  private final boolean initial;
  private final boolean terminal;
  private final boolean abstractt;
  private final boolean virtual;
  private final ActionGraph entryActionGraph;
  private final ActionGraph exitActionGraph;
  private final ActionGraph whileActionGraph;

  State(Parameters parameters) {
    if (parameters.baseState().isEmpty()) {
      this.parentStateMachineId = parameters.parentStateMachineId;
      this.name = parameters.name();

      this.localContextClass = parameters.localContextClass();

      this.initial = parameters.initial();
      this.terminal = parameters.terminal();

      this.entryActionGraph = ActionGraphBuilder.from(parameters.entryActions()).build();
      this.exitActionGraph = ActionGraphBuilder.from(parameters.exitActions()).build();
      this.whileActionGraph = ActionGraphBuilder.from(parameters.whileActions()).build();

      this.abstractt = parameters.abstractt();
      this.virtual = parameters.virtual();
    } else {
      var baseState = parameters.baseState().get();

      this.parentStateMachineId = baseState.parentStateMachineId;
      this.name = baseState.name;

      this.localContextClass = baseState.localContextClass;

      this.initial = parameters.initial() || baseState.initial;
      this.terminal = parameters.terminal() || baseState.terminal;

      this.entryActionGraph = ActionGraphBuilder.extend(new ActionGraph(baseState.entryActionGraph), parameters.entryActions()).build();
      this.exitActionGraph = ActionGraphBuilder.extend(new ActionGraph(baseState.exitActionGraph), parameters.exitActions()).build();
      this.whileActionGraph = ActionGraphBuilder.extend(new ActionGraph(baseState.whileActionGraph), parameters.whileActions()).build();

      this.abstractt = parameters.abstractt();

      // Ensure overridden abstract states are virtual if they are no longer abstract, so they can be further overridden
      this.virtual = (baseState.abstractt && !abstractt) || baseState.virtual;
    }
  }

  public UUID getParentStateMachineId() {
    return parentStateMachineId;
  }

  public String getName() {
    return name;
  }

  public boolean isInitial() {
    return initial;
  }

  public boolean isTerminal() {
    return terminal;
  }

  public boolean isAbstract() {
    return abstractt;
  }

  public boolean isVirtual() {
    return virtual;
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

  @Override
  public void accept(PlantUmlVisitor visitor) {
    visitor.visit(this);
  }

  record Parameters(
      UUID parentStateMachineId,
      String name,
      Optional<ContextClass> localContextClass,
      boolean initial,
      boolean terminal,
      List<Action> entryActions,
      List<Action> exitActions,
      List<Action> whileActions,
      boolean abstractt,
      boolean virtual,
      Optional<State> baseState
  ) {

  }
}
