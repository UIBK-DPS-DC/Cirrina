package at.ac.uibk.dps.cirrina.core.io.plantuml;

import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.core.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.core.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.core.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.core.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.core.object.state.State;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.core.object.transition.OnTransition;
import at.ac.uibk.dps.cirrina.core.object.transition.Transition;
import java.util.Objects;
import java.util.Optional;

public class PlantUmlVisitor {

  private final StringBuilder plantUml = new StringBuilder();
  private StringBuilder actionBuilder = new StringBuilder();
  private StringBuilder transitionBuilder = new StringBuilder();

  public String getPlantUml() {
    if (plantUml.isEmpty()) {
      throw new IllegalStateException("No plantUml generated, visit a state machine first.");
    }
    return plantUml.toString();
  }

  public void visit(StateMachine stateMachine) {
    plantUml.append("state ").append(stateMachine.getName()).append(" {\n");

    stateMachine.vertexSet().forEach(
        s -> {
          plantUml.append("    ");
          s.accept(this);
          plantUml.append("\n");
        });
    stateMachine.edgeSet().forEach(t -> {
      plantUml.append("    ");
      t.accept(this);
      plantUml.append(transitionBuilder);
      plantUml.append("\n");
    });

    stateMachine.getNestedStateMachines().forEach(sm -> {
      sm.accept(this);
      plantUml.append("\n");
    });

    plantUml.append("}\n");
  }

  public void visit(State state) {
    plantUml.append(String.format("state \"%s\" as %s%n    ", state.getName(), getStateId(state)));

    if (state.isInitial()) {
      plantUml.append(String.format("[*] --> %s%n    ", getStateId(state)));
    }
    if (state.isTerminal()) {
      plantUml.append(String.format("%s --> [*]%n    ", getStateId(state)));
    }
    if (!state.getEntryActionGraph().getActions().isEmpty()) {
      actionBuilder = new StringBuilder();
      state.getEntryActionGraph().getActions().forEach(action -> {
        action.accept(this);
        actionBuilder.append("; ");
      });
      plantUml.append(String.format("state \"%s\" as %s : entry / %s%n    ", state.getName(), getStateId(state), actionBuilder));
    }

    if (!state.getExitActionGraph().getActions().isEmpty()) {
      actionBuilder = new StringBuilder();
      state.getExitActionGraph().getActions().forEach(action -> {
        action.accept(this);
        actionBuilder.append("; ");
      });
      plantUml.append(String.format("state \"%s\" as %s : exit / %s%n    ", state.getName(), getStateId(state), actionBuilder));
    }

    if (!state.getWhileActionGraph().getActions().isEmpty()) {
      actionBuilder = new StringBuilder();
      var whileActions = state.getWhileActionGraph().getActions();
      whileActions.forEach(action -> {
        action.accept(this);
        actionBuilder.append("; ");
      });
      plantUml.append(String.format("state \"%s\" as %s : while / %s%n    ", state.getName(), getStateId(state), actionBuilder));
    }
  }

  public void visit(Action action) {
    switch (action) {
      case AssignAction a -> actionBuilder.append(String.format("<color:%s>%s{%s = %s}()</color>", ActionColors.getActionColor(a),
          a.getName().orElse("Assign"), a.getVariable().name(), a.getVariable().value()));
      case CreateAction a -> actionBuilder.append(String.format("<color:%s>%s()</color>", ActionColors.getActionColor(a),
          a.getName().orElse("Create")));
      case InvokeAction a -> actionBuilder.append(String.format("<color:%s>%s{%s}(%s)</color>", ActionColors.getActionColor(a),
          a.getName().orElse("Invoke"), a.getServiceType(), a.getInput()));
      case MatchAction a -> actionBuilder.append(String.format("<color:%s>%s()</color>", ActionColors.getActionColor(a),
          a.getName().orElse("Match")));
      case RaiseAction a -> actionBuilder.append(String.format("<color:%s>%s{%s}()</color>", ActionColors.getActionColor(a),
          a.getName().orElse("Raise"), a.getEvent()));
      case TimeoutAction a -> actionBuilder.append(String.format("<color:%s>%s()</color>", ActionColors.getActionColor(a),
          a.getName().orElse("Timeout")));
      case TimeoutResetAction a -> actionBuilder.append(String.format("<color:%s>%s()</color>", ActionColors.getActionColor(a),
          a.getName().orElse("TimeoutReset")));
      default -> throw new IllegalStateException("Unexpected action");
    }
  }

  public void visit(Transition transition) {
    transitionBuilder = new StringBuilder();
    if (Objects.requireNonNull(transition) instanceof OnTransition t) {
      transitionBuilder.append(buildPlantUml(t, Optional.of(t.getEventName())));
    } else {
      transitionBuilder.append(buildPlantUml(transition, Optional.empty()));
    }
  }

  protected StringBuilder buildPlantUml(Transition transition, Optional<String> eventName) {
    var descriptionBuilder = new StringBuilder();

    var guardBuilder = new StringBuilder();
    var guardList = transition.getGuards();
    if (!guardList.isEmpty()) {
      for (int i = 0; i < guardList.size() - 1; i++) {
        guardBuilder.append(guardList.get(i).getExpression().toString()).append(" && ");
      }
      guardBuilder.append(guardList.getLast().getExpression().toString());
    }

    var actionBuilder = new StringBuilder();
    if (!transition.getActionGraph().getActions().isEmpty()) {
      actionBuilder.append("\\n/ ");
      transition.getActionGraph().getActions().forEach(action -> actionBuilder.append(action.toString()).append("(); "));
    }

    var eventNameBuilder = new StringBuilder();
    var isOnTransition = eventName.isPresent();
    if (isOnTransition) {
      eventNameBuilder.append("**").append(eventName.get()).append("**");
    }

    var sourceStateId = getStateId(transition.getSource());
    var targetStateId = getStateId(transition.getTarget());

    if (!guardBuilder.isEmpty()) {
      descriptionBuilder.append(sourceStateId).append(getArrow(transition)).append(targetStateId).append(" : ").append(eventNameBuilder)
          .append("\\n[").append(guardBuilder).append("]").append(actionBuilder);
    } else {
      descriptionBuilder.append(sourceStateId).append(getArrow(transition)).append(targetStateId).append(" : ").append(eventNameBuilder)
          .append(actionBuilder);
    }

    return descriptionBuilder;
  }

  private String getArrow(Transition transition) {
    if (transition.getSource().getName().equals(transition.getTargetName())) {
      return " -[dashed]-> ";
    } else {
      return " --> ";
    }
  }

  private String getStateId(State state) {
    return state.getParentStateMachineId().toString().replace("-", "_") + "_" + state.getName();
  }
}
