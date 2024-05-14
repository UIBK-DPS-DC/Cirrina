package at.ac.uibk.dps.cirrina.classes.helper;

import at.ac.uibk.dps.cirrina.classes.statemachine.StateMachineClass;
import at.ac.uibk.dps.cirrina.csml.description.guard.GuardDescription;
import at.ac.uibk.dps.cirrina.csml.description.guard.GuardReferenceDescription;
import at.ac.uibk.dps.cirrina.csml.description.helper.GuardOrGuardReferenceDescription;
import at.ac.uibk.dps.cirrina.execution.object.guard.Guard;
import at.ac.uibk.dps.cirrina.execution.object.guard.GuardBuilder;
import java.util.Optional;

/**
 * Guard resolver, used to resolve a pre-created (named) guard object or construct an in-line guard object.
 */
public final class GuardResolver {

  /**
   * State machine class containing named guard objects.
   */
  private final StateMachineClass stateMachineClass;

  /**
   * Initializes this guard resolver instance.
   *
   * @param stateMachineClass The state machine class containing named guard objects.
   */
  public GuardResolver(StateMachineClass stateMachineClass) {
    this.stateMachineClass = stateMachineClass;
  }

  /**
   * Attempts to resolve a guard reference or construct an in-line guard object.
   * <p>
   * The provided description is expected to be either a guard or guard reference description, in case it is not, empty is returned.
   *
   * @param guardOrGuardReferenceDescription Guard reference or guard description.
   * @return Named guard object in case of a provided reference or an in-line constructed guard object.
   */
  public Optional<Guard> resolve(GuardOrGuardReferenceDescription guardOrGuardReferenceDescription) {
    switch (guardOrGuardReferenceDescription) {
      // An inline guard is provided as a guard class, since this guard is inline it needs to be constructed
      case GuardDescription guardClass -> {
        return Optional.of(GuardBuilder.from(guardClass).build());
      }
      // A guard reference is a reference to a named guard contained within the state machine, we provide this guard
      case GuardReferenceDescription guardReferenceClass -> {
        return stateMachineClass.findGuardByName(guardReferenceClass.reference);
      }
      default -> {
        return Optional.empty();
      }
    }
  }
}
