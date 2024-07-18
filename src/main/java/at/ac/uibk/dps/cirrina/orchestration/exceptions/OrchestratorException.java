package at.ac.uibk.dps.cirrina.orchestration.exceptions;

public class OrchestratorException extends Exception {

  public OrchestratorException(String message) {
    super(message);
  }

  public OrchestratorException(String message, Throwable cause) {
    super(message, cause);
  }
}
