package at.ac.uibk.dps.cirrina.core.exception;


public final class VerificationException extends CirrinaException {

  public final Message message;

  private VerificationException(Message message, Object... args) {
    super(String.format("%s (Error): ", message.number) + String.format(message.message, args));
    this.message = message;
  }

  public static VerificationException from(Message message, Object... args) {
    return new VerificationException(message, args);
  }

  public enum Message {
    STATE_NAME_DOES_NOT_EXIST(0, "A state with the name '%s' does not exist"),
    ILLEGAL_STATE_MACHINE_GRAPH(1, "The edge between states '%s' and '%s' is illegal"),
    MULTIPLE_TRANSITIONS_WITH_SAME_EVENT(2, "State '%s' has multiple outwards transitions with the same event"),
    NAMED_ACTION_DOES_NOT_EXIST(4, "State machine '%s' does not have a named action with the name '%s'"),
    ACTION_NAME_IS_NOT_UNIQUE(5, "Action name '%s' is not unique"),
    STATE_NAME_IS_NOT_UNIQUE(6, "State name '%s' is not unique"),
    STATE_MACHINE_INHERITS_FROM_INVALID(7, "State machine '%s' cannot inherit from '%s', state machine does not exist"),
    STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES(8, "State machine '%s' overrides states which are neither abstract nor virtual"),
    STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES(9, "State machine '%s' does not override all abstract states of state machine '%s'"),
    NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES(10, "State machine '%s' has abstract states but is not defined as abstract"),
    EXPRESSION_COULD_NOT_BE_PARSED(11, "The expression '%s' could not be parsed: %s"),
    STATE_MACHINE_DOES_NOT_HAVE_ONE_INITIAL_STATE(12, "State machine '%s' does not have exactly one initial state");

    public final int number;

    public final String message;

    Message(int number, String message) {
      this.number = number;
      this.message = message;
    }
  }
}
