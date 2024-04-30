package at.ac.uibk.dps.cirrina.execution.instance.transition;

import at.ac.uibk.dps.cirrina.core.object.transition.Transition;

public final class TransitionInstance {

  private final Transition transition;

  public TransitionInstance(Transition transition) {
    this.transition = transition;
  }

  public Transition getTransition() {
    return transition;
  }
}
