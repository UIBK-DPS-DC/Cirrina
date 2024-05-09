package at.ac.uibk.dps.cirrina.core.lang.parser;

import at.ac.uibk.dps.cirrina.core.lang.classes.CollaborativeStateMachineClass;

/**
 * CSML parser. Provides parsing functionality for descriptions written in the CSML language.
 * A description is parsed into a structure consisting of CSML models.
 */
public class CollaborativeStateMachineParser extends Parser<CollaborativeStateMachineClass> {

  /**
   * Initializes the parser.
   */
  public CollaborativeStateMachineParser() {
    super(CollaborativeStateMachineClass.class);
  }
}
