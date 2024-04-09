package at.ac.uibk.dps.cirrina.core.object.helper;

import at.ac.uibk.dps.cirrina.core.lang.classes.guard.GuardClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.guard.GuardReferenceClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.helper.GuardOrGuardReferenceClass;
import at.ac.uibk.dps.cirrina.core.object.guard.Guard;
import at.ac.uibk.dps.cirrina.core.object.guard.GuardBuilder;
import at.ac.uibk.dps.cirrina.core.object.statemachine.StateMachine;
import java.util.Optional;

public final class GuardResolver {

  private final StateMachine stateMachine;

  public GuardResolver(StateMachine stateMachine) {
    this.stateMachine = stateMachine;
  }

  public Optional<Guard> resolve(GuardOrGuardReferenceClass guardOrGuardReferenceClass) throws IllegalStateException {
    switch (guardOrGuardReferenceClass) {
      // An inline action is provided as an action class, since this action is inline it needs to be constructed
      case GuardClass guardClass -> {
        return Optional.of(GuardBuilder.from(guardClass).build());
      }
      // An action reference is a reference to a named action contained within the state machine, we provide this action
      case GuardReferenceClass actionReferenceClass -> {
        return stateMachine.findGuardByName(actionReferenceClass.reference);
      }
      default -> {
        return Optional.empty();
      }
    }
  }
}
