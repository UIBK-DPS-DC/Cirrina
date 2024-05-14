package at.ac.uibk.dps.cirrina.csml.description;

import at.ac.uibk.dps.cirrina.csml.description.action.ActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.ActionReferenceDescription;
import at.ac.uibk.dps.cirrina.csml.description.context.ContextDescription;
import at.ac.uibk.dps.cirrina.csml.description.guard.GuardDescription;
import at.ac.uibk.dps.cirrina.csml.description.guard.GuardReferenceDescription;
import at.ac.uibk.dps.cirrina.csml.description.helper.StateOrStateMachineDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

/**
 * StateClass machine construct. Represents a state machine within a collaborative state machine.
 * <p>
 * Keywords:
 * <table border="1">
 *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
 *  <tr><td>name</td><td>Unique name</td><td>Yes</td></tr>
 *  <tr><td>localContext</td><td>Lexical description of the local context</td><td>No</td></tr>
 *  <tr><td>persistentContext</td><td>Lexical description of the persistent context</td><td>No</td></tr>
 *  <tr><td>guards</td><td>Named guards</td><td>No</td></tr>
 *  <tr><td>actions</td><td>Named actions</td><td>No</td></tr>
 *  <tr><td>abstract</td><td>Abstract state machine flag</td><td>No</td></tr>
 * </table>
 * <p>
 * Example:
 * <pre>
 * {
 *   name: 'Collaborative StateClass Machine Name',
 *   states: [...],
 *   localContext: [...],
 *   persistentContext: [...],
 *   guards: [],
 *   actions: [],
 *   abstract: false
 * }
 * </pre>
 *
 * @since CSML 0.1.
 */
public final class StateMachineDescription extends Construct implements StateOrStateMachineDescription {

  /**
   * The name.
   */
  @NotNull
  public String name = "";

  /**
   * The states.
   * <p>
   * At least one initial state must be provided.
   * </p>
   */
  @NotNull
  @Size(min = 1, message = "At least one state must be provided in 'states'")
  public List<StateOrStateMachineDescription> states;

  /**
   * The optional lexical declaration of local context variables.
   */
  public Optional<ContextDescription> localContext = Optional.empty();

  /**
   * The optional lexical declaration of persistent context variables.
   */
  public Optional<ContextDescription> persistentContext = Optional.empty();

  /**
   * The optional named guards.
   * <p>
   * The guards declared here may be used inside this state machine by referencing the names.
   * </p>
   *
   * @see StateMachineDescription
   * @see GuardReferenceDescription
   */
  public List<GuardDescription> guards = List.of();

  /**
   * The optional named actions.
   * <p>
   * The actions declared here may be used inside this state machine by referencing the names.
   * </p>
   *
   * @see StateMachineDescription
   * @see ActionReferenceDescription
   */
  public List<ActionDescription> actions = List.of();

  /**
   * The optional extended state machine name.
   * <p>
   * The state machine extends this state machine, copies all its properties and allows them to be overridden. States can only be overridden
   * if they are defined as virtual or abstract.
   * </p>
   *
   * @see StateDescription
   */
  @JsonProperty("extends")
  public Optional<String> extendss = Optional.empty();

  /**
   * The optional abstract modifier.
   * <p>
   * If a state machine is defined as abstract, it cannot be instantiated and is never executed by nested state machines. Abstract state
   * machines can be extended and can have abstract states.
   * </p>
   *
   * @see StateDescription
   */
  @JsonSetter(nulls = Nulls.SKIP)
  @JsonProperty("abstract")
  public boolean abstractt = false;
}
