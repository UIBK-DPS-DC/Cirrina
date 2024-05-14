package at.ac.uibk.dps.cirrina.classes.helper;

import at.ac.uibk.dps.cirrina.classes.statemachine.StateMachineClass;
import at.ac.uibk.dps.cirrina.csml.description.action.ActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.ActionReferenceDescription;
import at.ac.uibk.dps.cirrina.csml.description.helper.ActionOrActionReferenceDescription;
import at.ac.uibk.dps.cirrina.execution.object.action.Action;
import at.ac.uibk.dps.cirrina.execution.object.action.ActionBuilder;
import java.util.Optional;

/**
 * Action resolver, used to resolve a pre-created (named) action object or construct an in-line action object.
 */
public final class ActionResolver {

  /**
   * State machine class containing named action objects.
   */
  private final StateMachineClass stateMachineClass;

  /**
   * Initializes this action resolver instance.
   *
   * @param stateMachineClass The state machine class containing named action objects.
   */
  public ActionResolver(StateMachineClass stateMachineClass) {
    this.stateMachineClass = stateMachineClass;
  }

  /**
   * Attempts to resolve an action reference or construct an in-line action object.
   * <p>
   * The provided description is expected to be either an action or action reference description, in case it is not, empty is returned.
   *
   * @param actionOrActionReferenceDescription Action reference or action description.
   * @return Named action object in case of a provided reference or an in-line constructed action object.
   */
  public Optional<Action> tryResolve(ActionOrActionReferenceDescription actionOrActionReferenceDescription) {
    switch (actionOrActionReferenceDescription) {
      // An inline action is provided as an action class, since this action is inline it needs to be constructed
      case ActionDescription actionDescription -> {
        return Optional.of(ActionBuilder.from(actionDescription, this).build());
      }
      // An action reference is a reference to a named action contained within the state machine, we provide this action
      case ActionReferenceDescription actionReferenceClass -> {
        return stateMachineClass.findActionByName(actionReferenceClass.reference);
      }
      default -> {
        return Optional.empty();
      }
    }
  }
}
