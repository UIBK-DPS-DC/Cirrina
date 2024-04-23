package at.ac.uibk.dps.cirrina.core.exception;

/**
 * Cirrina exception, base exception.
 */
public class CirrinaException extends Exception {

  /**
   * Initialize this cirrina exception object with a formatted message.
   *
   * @param format Message format.
   * @param args   Arguments.
   */
  protected CirrinaException(String format, Object... args) {
    super(String.format(format, args));
  }

  /**
   * Construct an exception object given a format string.
   *
   * @param format Format string.
   * @param args   Arguments.
   * @return Exception object.
   */
  public static CirrinaException from(String format, Object... args) {
    return new CirrinaException(format, args);
  }
}