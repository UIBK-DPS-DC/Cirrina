package at.ac.uibk.dps.cirrina.core.exception;

/**
 * Verification exception, used to report pre-defined verification errors.
 */
public final class VerificationException extends CirrinaException {

  /**
   * Verification error message.
   */
  public final Message message;

  /**
   * Initializes this verification exception object.
   *
   * @param message
   * @param args
   */
  private VerificationException(Message message, Object... args) {
    super("Verification failed - %s", String.format(message.message, args));

    this.message = message;
  }

  /**
   * Construct a verification exception given a pre-defined message.
   *
   * @param message
   * @param args
   * @return
   */
  public static VerificationException from(Message message, Object... args) {
    return new VerificationException(message, args);
  }

  /**
   * Error message.
   */
  public enum Message {
    ILLEGAL_STATE_MACHINE_GRAPH("The edge between states '%s' and '%s' is illegal: %s"),
    MULTIPLE_TRANSITIONS_WITH_SAME_EVENT("Multiple outwards transitions with the same event: %s"),
    GUARD_NAME_IS_NOT_UNIQUE("Guard name(s) '%s' is/are not unique: %s"),
    ACTION_NAME_IS_NOT_UNIQUE("Action name(s) '%s' is/are not unique: %s"),
    STATE_NAME_IS_NOT_UNIQUE("StateClass name(s) '%s' is/are not unique: %s"),
    GUARD_NAME_DOES_NOT_EXIST("Guard with name '%s' does not exist"),
    ACTION_NAME_DOES_NOT_EXIST("Action with name '%s' does not exist"),
    STATE_MACHINE_EXTENDS_INVALID("Cannot extend '%s', state machine does not exist"),
    STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES("States overridden which are neither abstract nor virtual: %s"),
    STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES("Not all abstract states are overridden: %s"),
    NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES("States are abstract but state machine is not abstract: %s"),
    EXPRESSION_COULD_NOT_BE_PARSED("The expression '%s' could not be parsed: %s"),
    STATE_MACHINE_HAS_MULTIPLE_INITIAL_STATES("Multiple initial states '%s': %s"),
    STATE_MACHINE_DOES_NOT_HAVE_ONE_INITIAL_STATE("No initial state: %s"),
    AFTER_ACTION_IS_NOT_A_TIMEOUT_ACTION("After action is not a timeout action"),
    TIMEOUT_ACTION_NAME_IS_NOT_PROVIDED("Timeout action name is not provided");

    /**
     * Messaging string.
     */
    public final String message;

    /**
     * Initializes this message object.
     *
     * @param message Message string.
     */
    private Message(String message) {
      this.message = message;
    }
  }
}
