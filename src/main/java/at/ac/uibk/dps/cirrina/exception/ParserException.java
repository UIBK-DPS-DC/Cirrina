package at.ac.uibk.dps.cirrina.exception;

public final class ParserException extends CirrinaException {

  private ParserException(String message, Object... args) {
    super(String.format(message, args));
  }

  public static ParserException from(String message, Object... args) {
    return new ParserException(message, args);
  }
}
