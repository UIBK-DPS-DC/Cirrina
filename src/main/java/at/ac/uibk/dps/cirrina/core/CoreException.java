package at.ac.uibk.dps.cirrina.core;

import at.ac.uibk.dps.cirrina.CirrinaException;

public final class CoreException extends CirrinaException {

  private CoreException(String message, Object... args) {
    super(String.format(message, args));
  }

  public static CoreException from(String message, Object... args) {
    return new CoreException(message, args);
  }
}
