package at.ac.uibk.dps.cirrina.core.exception;

public final class RuntimeException extends CirrinaException {

  private RuntimeException(String message, Object... args) {
    super(String.format(message, args));
  }

  public static RuntimeException from(String message, Object... args) {
    return new RuntimeException(message, args);
  }
}
