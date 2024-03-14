package at.ac.uibk.dps.cirrina.runtime.helper;

import at.ac.uibk.dps.cirrina.lang.classes.action.ActionClass;
import at.ac.uibk.dps.cirrina.lang.classes.action.ActionOrActionReferenceClass;
import at.ac.uibk.dps.cirrina.lang.classes.action.ActionReferenceClass;
import at.ac.uibk.dps.cirrina.runtime.action.Action;
import at.ac.uibk.dps.cirrina.runtime.action.ActionBuilder;
import at.ac.uibk.dps.cirrina.runtime.statemachine.StateMachine;

public final class ActionResolver {

  private final StateMachine stateMachine;

  public ActionResolver(StateMachine stateMachine) {
    this.stateMachine = stateMachine;
  }

  public Action resolve(ActionOrActionReferenceClass actionOrActionReferenceClass) throws IllegalStateException {
    switch (actionOrActionReferenceClass) {
      // An inline action is provided as an action class, since this action is inline it needs to be constructed
      case ActionClass actionClass -> {
        return ActionBuilder.from(actionClass).build();
      }
      // An action reference is a reference to a named action contained within the state machine, we provide this action
      case ActionReferenceClass actionReferenceClass -> {
        return this.stateMachine.getActionByName(actionReferenceClass.reference);
      }
      default -> throw new IllegalStateException(String.format("Unexpected value: ", actionOrActionReferenceClass));
    }
  }
}
