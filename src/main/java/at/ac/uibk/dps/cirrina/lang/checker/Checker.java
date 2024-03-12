package at.ac.uibk.dps.cirrina.lang.checker;

import at.ac.uibk.dps.cirrina.core.objects.CollaborativeStateMachine;
import at.ac.uibk.dps.cirrina.core.objects.builder.CollaborativeStateMachineBuilder;
import at.ac.uibk.dps.cirrina.lang.parser.classes.CollaborativeStateMachineClass;

public final class Checker {

  private final Options options;

  public Checker(Options options) {
    this.options = options;
  }

  public CollaborativeStateMachine check(
      CollaborativeStateMachineClass collaborativeStateMachineClass) throws CheckerException {
    try {
      return new CollaborativeStateMachineBuilder(collaborativeStateMachineClass).build();
    } catch (IllegalArgumentException e) {
      // We expect a checker exception which is the cause of the exception caught, in case we don't get a checker
      // exception as the cause, we don't know what to do and just rethrow. Otherwise, we throw the CheckerException
      var cause = e.getCause();
      if (!(cause instanceof CheckerException checkerException)) {
        throw e;
      }

      throw checkerException;
    }
  }

  public record Options() {

  }

}
