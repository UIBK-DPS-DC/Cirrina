package ac.at.uibk.dps.nexa.lang.checker;

import ac.at.uibk.dps.nexa.core.objects.CollaborativeStateMachine;
import ac.at.uibk.dps.nexa.core.objects.builder.CollaborativeStateMachineBuilder;
import ac.at.uibk.dps.nexa.lang.parser.classes.CollaborativeStateMachineClass;

public class Checker {

  private final Options options;

  public Checker(Options options) {
    this.options = options;
  }

  public CollaborativeStateMachine check(CollaborativeStateMachineClass collaborativeStateMachineClass)
      throws CheckerException {
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
