package ac.at.uibk.dps.nexa.lang.parser.classes;

import ac.at.uibk.dps.nexa.lang.parser.classes.helper.GuardOrGuardReferenceClass;
import jakarta.validation.constraints.NotNull;

/**
 * Guard reference construct. Represents a reference (by name) to an existing guard.
 * <p>
 * Keywords:
 * <table border="1">
 *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
 *  <tr><td>reference</td><td>Unique name</td><td>Yes</td></tr>
 * </table>
 * </p>
 *  Example:
 *  <pre>
 *    {
 *      reference: 'Guard Name'
 *    }
 *  </pre>
 * </p>
 *
 * @since CSML 0.1.
 */
public class GuardReferenceClass extends Construct implements GuardOrGuardReferenceClass {

  /**
   * The guard name reference.
   * <p>
   * Must be the name of an existing guard. Guard references may be declared as part of a state machine.
   * </p>
   *
   * @see StateMachineClass
   */
  @NotNull
  public String reference;
}
