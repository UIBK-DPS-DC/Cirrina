package at.ac.uibk.dps.cirrina.lang.checker;

import at.ac.uibk.dps.cirrina.CirrinaException;


public final class CheckerException extends CirrinaException {

  public CheckerException(Message message, Object... args) {
    super(String.format("%s (%d): ", message.error ? "Error" : "Warning", message.number)
        + String.format(
        message.message, args));
  }

  public enum Message {
    STATE_NAME_DOES_NOT_EXIST(0, "A state with the name '%s' does not exist", true),
    ILLEGAL_STATE_MACHINE_GRAPH(1, "The edge between states '%s' and '%s' is illegal", true),
    MULTIPLE_TRANSITIONS_WITH_SAME_EVENT(2,
        "State '%s' has multiple outwards transitions with the same event", true),
    NAMED_ACTION_DOES_NOT_EXIST(4,
        "State machine '%s' does not have a named action with the name '%s'", true),
    ACTION_NAME_IS_NOT_UNIQUE(5, "Action name '%s' is not unique", true),
    STATE_NAME_IS_NOT_UNIQUE(6, "State name '%s' is not unique", true),
    STATE_MACHINE_INHERITS_FROM_INVALID(7,
        "State machine '%s' cannot inherit from '%s', state machine does not exist",
        true),
    STATE_MACHINE_OVERRIDES_UNSUPPORTED_STATES(8,
        "State machine '%s' overrides states which are neither abstract nor virtual", true),
    STATE_MACHINE_DOES_NOT_OVERRIDE_ABSTRACT_STATES(9,
        "State machine '%s' does not override all abstract states of state machine '%s'", true),
    NON_ABSTRACT_STATE_MACHINE_HAS_ABSTRACT_STATES(10,
        "State machine '%s' has abstract states but is not defined as abstract", true),
    EVENT_IS_HANDLED_BUT_NOT_RAISED(11,
        "The event '%s' is handled but not raised", true);

    public final int number;

    public final String message;

    public final boolean error;

    Message(int number, String message, boolean error) {
      this.number = number;
      this.message = message;
      this.error = error;
    }
  }
}
