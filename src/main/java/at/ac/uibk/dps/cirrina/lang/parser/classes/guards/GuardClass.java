package at.ac.uibk.dps.cirrina.lang.parser.classes.guards;

import at.ac.uibk.dps.cirrina.lang.parser.classes.Construct;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateMachineClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.helper.GuardOrGuardReferenceClass;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Guard construct. Represents a conditional (if) that determines if a transition can be taken.
 * Guards can be declared and referenced as part of a state machine, or be declared inline.
 * <p>
 * Keywords:
 * <table border="1">
 *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
 *  <tr><td>name</td><td>Unique name</td><td>Yes</td></tr>
 *  <tr><td>expression</td><td>Expression</td><td>Yes</td></tr>
 * </table>
 * </p>
 * <p>
 * Example:
 * <pre>
 *   {
 *     name: 'Guard Name',
 *     expression: 'a==5'
 *   }
 * </pre>
 * </p>
 *
 * @since CSML 0.1.
 */
public class GuardClass extends Construct implements GuardOrGuardReferenceClass {

  /**
   * An expression.
   * <p>
   * The expression must evaluate to a boolean value.
   * </p>
   */
  @NotNull
  public String expression;

  /**
   * The optional name.
   * <p>
   * If present, can be referenced from within a state machine component when declared as part of
   * the state machine's guards.
   *
   * @see StateMachineClass
   * @see GuardReferenceClass
   */
  public Optional<String> name;
}
