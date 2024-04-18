package at.ac.uibk.dps.cirrina.core.lang.classes;

import at.ac.uibk.dps.cirrina.core.lang.classes.context.ContextClass;
import at.ac.uibk.dps.cirrina.core.lang.keyword.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

/**
 * Collaborative state machine construct. Represents the highest level entity in a description.
 * <p>
 * Keywords:
 * <table border="1">
 *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
 *  <tr><td>name</td><td>Unique name</td><td>Yes</td></tr>
 *  <tr><td>version</td><td>CSML version</td><td>Yes</td></tr>
 *  <tr><td>stateMachines</td><td>State machines</td><td>Yes (at least one)</td></tr>
 *  <tr><td>localContext</td><td>Lexical description of the local context</td><td>No</td></tr>
 *  <tr><td>persistentContext</td><td>Lexical description of the persistent context</td><td>No</td></tr>
 * </table>
 * <p>
 * Example:
 * <pre>
 * {
 *   name: 'Collaborative State Machine Name',
 *   version: '0.1',
 *   stateMachines: [...],
 *   localContext: [...],
 *   persistentContext: [...]
 * }
 * </pre>
 *
 * @since CSML 0.1.
 */
public final class CollaborativeStateMachineClass extends Construct {

  /**
   * The name.
   */
  @NotNull
  public String name;

  /**
   * The CSML version.
   * <p>
   * The following CSML versions are valid:
   * <table border="1">
   *   <tr><th>Version</th><th>Value</th></tr>
   *   <tr><td>Version 0.1</td><td>0.1</td></tr>
   * </table>
   * </p>
   */
  @NotNull
  public Version version;

  /**
   * The state machines.
   * <p>
   * At least one state machine must be provided.
   * </p>
   */
  @NotNull
  @Size(min = 1, message = "At least one state machine must be provided in 'stateMachines'")
  public List<StateMachineClass> stateMachines;

  /**
   * The optional lexical declaration of local context variables.
   */
  public Optional<ContextClass> localContext = Optional.empty();

  /**
   * The optional lexical declaration of persistent context variables.
   */
  public Optional<ContextClass> persistentContext = Optional.empty();
}
