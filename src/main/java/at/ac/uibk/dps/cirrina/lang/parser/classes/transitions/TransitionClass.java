package at.ac.uibk.dps.cirrina.lang.parser.classes.transitions;

import at.ac.uibk.dps.cirrina.lang.parser.classes.Construct;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateMachineClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.ActionOrActionReferenceClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.guards.GuardReferenceClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.helper.GuardOrGuardReferenceClass;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;

/**
 * Transition construct. Represents a transition that is to be taken regardless of an event.
 * <p>
 * Keywords:
 * <table border="1">
 *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
 *  <tr><td>target</td><td>Target state</td><td>Yes</td></tr>
 *  <tr><td>guards</td><td>Guards</td><td>Yes</td></tr>
 *  <tr><td>actions</td><td>Actions</td><td>Yes</td></tr>
 * </table>
 * <p>
 * Example:
 * <pre>
 * {
 *   target: 'State Name',
 *   guards: [...],
 *   actions: [...]
 * }
 * </pre>
 *
 * @since CSML 0.1.
 */
public class TransitionClass extends Construct {

  @NotNull
  public String target;

  /**
   * The optional guards. All guard expression need to evaluate to true before a transitions can be
   * taken. Can be provided as guard references to previously declared guards, or inline guards.
   *
   * @see StateMachineClass
   * @see GuardReferenceClass
   */
  public List<GuardOrGuardReferenceClass> guards = List.of();

  /**
   * The optional actions. These actions are executed during the transition, if the transition is
   * taken. Can be provided as action references to previously declared actions, or inline actions.
   *
   * @see StateMachineClass
   * @see GuardReferenceClass
   */
  public List<ActionOrActionReferenceClass> actions = List.of();

  /**
   * The optional else target. If the guards evaluate to false, the state machine ends up in this
   * target state.
   */
  @JsonProperty("else")
  public Optional<String> elsee = Optional.empty();
}
