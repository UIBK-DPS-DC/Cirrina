package at.ac.uibk.dps.cirrina.execution.command;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;

/**
 * Command, represents the interface to commands that can be entered into a state machine's command queue. Commands can be executed and may
 * produce new commands and have side effects.
 */
public abstract class Command {

  protected final ExecutionContext executionContext;

  public Command(ExecutionContext executionContext) {
    this.executionContext = executionContext;
  }

  public abstract void execute() throws CirrinaException;
}
