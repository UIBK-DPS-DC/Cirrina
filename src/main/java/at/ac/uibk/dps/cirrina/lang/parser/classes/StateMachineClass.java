package at.ac.uibk.dps.cirrina.lang.parser.classes;

import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.ActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.ActionReferenceClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.helper.StateOrStateMachineClass;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * State machine construct. Represents a state machine within a collaborative state machine.
 * <p>
 * Keywords:
 * <table border="1">
 *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
 *  <tr><td>name</td><td>Unique name</td><td>Yes</td></tr>
 *  <tr><td>localContext</td><td>Lexical description of the local context</td><td>No</td></tr>
 *  <tr><td>persistentContext</td><td>Lexical description of the persistent context</td><td>No</td></tr>
 *  <tr><td>guards</td><td>Named guards</td><td>No</td></tr>
 *  <tr><td>actions</td><td>Named actions</td><td>No</td></tr>
 * </table>
 * </p>
 * <p>
 *  Example:
 *  <pre>
 *    {
 *      name: 'Collaborative State Machine Name',
 *      states: [...],
 *      localContext: {
 *        variable: 'value'
 *      },
 *      persistentContext: {
 *        variable: 'value'
 *      },
 *      guards: [],
 *      actions: []
 *    }
 *  </pre>
 * </p>
 *
 * @since CSML 0.1.
 */
public class StateMachineClass extends Construct implements StateOrStateMachineClass {

  /**
   * The name.
   */
  @NotNull
  public String name;

  /**
   * The states.
   * <p>
   * At least one initial state must be provided.
   * </p>
   */
  @NotNull
  @Size(min = 1, message = "At least one state must be provided in 'states'")
  public List<StateOrStateMachineClass> states;

  /**
   * The optional lexical declaration of local context variables.
   */
  public Optional<Map<String, String>> localContext = Optional.empty();

  /**
   * The optional lexical declaration of persistent context variables.
   */
  public Optional<Map<String, String>> persistentContext = Optional.empty();

  /**
   * The optional named guards.
   * <p>
   * The guards declared here may be used inside this state machine by referencing the names.
   * </p>
   *
   * @see StateMachineClass
   * @see GuardReferenceClass
   */
  public Optional<List<GuardClass>> guards = Optional.empty();

  /**
   * The optional named actions.
   * <p>
   * The actions declared here may be used inside this state machine by referencing the names.
   * </p>
   *
   * @see StateMachineClass
   * @see ActionReferenceClass
   */
  public Optional<List<ActionClass>> actions = Optional.empty();

  /**
   * The optional inherited state machine name.
   * <p>
   * The state machine inherits from this state machine, copies all its properties and allows them to be overwritten.
   * States can only be overridden if they are defined as virtual or abstract.
   * </p>
   *
   * @see StateClass
   */
  public Optional<String> inherit = Optional.empty();

  /**
   * The optional abstract modifier.
   * <p>
   * If a state machine is defined as abstract, it cannot be instantiated and is never executed by nested state
   * machines. Abstract state machines can be inherited and can have abstract states.
   * </p>
   *
   * @see StateClass
   */
  @JsonSetter(nulls = Nulls.SKIP)
  public boolean isAbstract = false;
}
