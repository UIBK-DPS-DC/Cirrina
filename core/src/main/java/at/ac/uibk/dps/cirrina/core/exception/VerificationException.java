package at.ac.uibk.dps.cirrina.core.exception;


public final class VerificationException extends CirrinaException {

  public final Message message;

  private VerificationException(Message message, Object... args) {
    super("(Error): " + String.format(message.message, args));
    this.message = message;
  }

  public static VerificationException from(Message message, Object... args) {
    return new VerificationException(message, args);
  }

  public enum Message {
    ILLEGAL_STATE_MACHINE_GRAPH("The edge between states '%s' and '%s' is illegal: %s"),
    MULTIPLE_TRANSITIONS_WITH_SAME_EVENT("Multiple outwards transitions with the same event: %s"),
    GUARD_NAME_IS_NOT_UNIQUE("Guard name(s) '%s' is/are not unique: %s"),
    ACTION_NAME_IS_NOT_UNIQUE("Action name(s) '%s' is/are not unique: %s"),
    STATE_NAME_IS_NOT_UNIQUE("State name(s) '%s' is/are not unique: %s"),
    GUARD_NAME_DOES_NOT_EXIST("Guard with name '%s' does not exist"),
    ACTION_NAME_DOES_NOT_EXIST("Action with name '%s' does not exist"),
    STATE_MACHINE_INHERITS_FROM_INVALID("Cannot inherit from '%s', state machine does not exist"),
    STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES("States overridden which are neither abstract nor virtual: %s"),
    STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES("Not all abstract states of are overridden: %s"),
    NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES("States are abstract but state machine is not abstract: %s"),
    EXPRESSION_COULD_NOT_BE_PARSED("The expression '%s' could not be parsed: %s"),
    STATE_MACHINE_HAS_MULTIPLE_INITIAL_STATES("Multiple initial states '%s': %s"),
    STATE_MACHINE_DOES_NOT_HAVE_ONE_INITIAL_STATE("No initial state: %s");

    public final String message;

    Message(String message) {
      this.message = message;
    }
  }
}
