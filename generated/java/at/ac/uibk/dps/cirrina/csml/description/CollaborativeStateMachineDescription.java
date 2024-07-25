package at.ac.uibk.dps.cirrina.csml.description;

import java.util.List;
import java.util.Objects;
import org.pkl.config.java.mapper.Named;
import org.pkl.config.java.mapper.NonNull;

/**
 * Collaborative state machine construct. Represents the highest level entity in a description.
 * <p>
 * Keywords:
 * <table border="1">
 *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
 *  <tr><td>name</td><td>Unique name</td><td>Yes</td></tr>
 *  <tr><td>version</td><td>CSML version</td><td>Yes</td></tr>
 *  <tr><td>stateMachines</td><td>StateClass machines</td><td>Yes (at least one)</td></tr>
 *  <tr><td>localContext</td><td>Lexical description of the local context</td><td>No</td></tr>
 *  <tr><td>persistentContext</td><td>Lexical description of the persistent context</td><td>No</td></tr>
 * </table>
 * <p>
 * Example:
 * <pre>
 * {
 *   name: 'Collaborative State Machine Name',
 *   version: '0.1',
 *   stateMachines: [...]
 * }
 * </pre>
 *
 * @since CSML 0.1.
 */
public final class CollaborativeStateMachineDescription {

  private final @NonNull String name;

  private final @NonNull Version version;

  private final @NonNull List<@NonNull StateMachineDescription> stateMachines;

  private final ContextDescription localContext;

  private final ContextDescription persistentContext;

  public CollaborativeStateMachineDescription(@Named("name") @NonNull String name,
      @Named("version") @NonNull Version version,
      @Named("stateMachines") @NonNull List<@NonNull StateMachineDescription> stateMachines,
      @Named("localContext") ContextDescription localContext,
      @Named("persistentContext") ContextDescription persistentContext) {
    this.name = name;
    this.version = version;
    this.stateMachines = stateMachines;
    this.localContext = localContext;
    this.persistentContext = persistentContext;
  }

  private static void appendProperty(StringBuilder builder, String name, Object value) {
    builder.append("\n  ").append(name).append(" = ");
    String[] lines = Objects.toString(value).split("\n");
    builder.append(lines[0]);
    for (int i = 1; i < lines.length; i++) {
      builder.append("\n  ").append(lines[i]);
    }
  }

  /**
   * The name.
   */
  public @NonNull String getName() {
    return name;
  }

  public CollaborativeStateMachineDescription withName(@NonNull String name) {
    return new CollaborativeStateMachineDescription(name, version, stateMachines, localContext, persistentContext);
  }

  /**
   * The CSML version.
   * <p>
   * The following CSML versions are valid:
   * <table border="1">
   *   <tr><th>Version</th><th>Value</th></tr>
   *   <tr><td>Version 0.1</td><td>0.1</td></tr>
   * </table>
   */
  public @NonNull Version getVersion() {
    return version;
  }

  public CollaborativeStateMachineDescription withVersion(@NonNull Version version) {
    return new CollaborativeStateMachineDescription(name, version, stateMachines, localContext, persistentContext);
  }

  /**
   * The state machines.
   * <p>
   * At least one state machine must be provided.
   */
  public @NonNull List<@NonNull StateMachineDescription> getStateMachines() {
    return stateMachines;
  }

  public CollaborativeStateMachineDescription withStateMachines(
      @NonNull List<@NonNull StateMachineDescription> stateMachines) {
    return new CollaborativeStateMachineDescription(name, version, stateMachines, localContext, persistentContext);
  }

  /**
   * The optional lexical declaration of local context variables.
   */
  public ContextDescription getLocalContext() {
    return localContext;
  }

  public CollaborativeStateMachineDescription withLocalContext(ContextDescription localContext) {
    return new CollaborativeStateMachineDescription(name, version, stateMachines, localContext, persistentContext);
  }

  /**
   * The optional lexical declaration of persistent context variables.
   */
  public ContextDescription getPersistentContext() {
    return persistentContext;
  }

  public CollaborativeStateMachineDescription withPersistentContext(
      ContextDescription persistentContext) {
    return new CollaborativeStateMachineDescription(name, version, stateMachines, localContext, persistentContext);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    CollaborativeStateMachineDescription other = (CollaborativeStateMachineDescription) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.version, other.version)) {
      return false;
    }
    if (!Objects.equals(this.stateMachines, other.stateMachines)) {
      return false;
    }
    if (!Objects.equals(this.localContext, other.localContext)) {
      return false;
    }
    if (!Objects.equals(this.persistentContext, other.persistentContext)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(this.name);
    result = 31 * result + Objects.hashCode(this.version);
    result = 31 * result + Objects.hashCode(this.stateMachines);
    result = 31 * result + Objects.hashCode(this.localContext);
    result = 31 * result + Objects.hashCode(this.persistentContext);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(300);
    builder.append(CollaborativeStateMachineDescription.class.getSimpleName()).append(" {");
    appendProperty(builder, "name", this.name);
    appendProperty(builder, "version", this.version);
    appendProperty(builder, "stateMachines", this.stateMachines);
    appendProperty(builder, "localContext", this.localContext);
    appendProperty(builder, "persistentContext", this.persistentContext);
    builder.append("\n}");
    return builder.toString();
  }

  public enum Version {
    _0_1("0.1"),

    _0_2("0.2");

    private String value;

    private Version(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }
  }

  public enum Type {
    INVOKE("invoke"),

    CREATE("create"),

    ASSIGN("assign"),

    LOCK("lock"),

    UNLOCK("unlock"),

    RAISE("raise"),

    TIMEOUT("timeout"),

    TIMEOUT_RESET("timeoutReset"),

    MATCH("match");

    private String value;

    private Type(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }
  }

  public enum EventChannel {
    INTERNAL("internal"),

    EXTERNAL("external"),

    GLOBAL("global"),

    PERIPHERAL("peripheral");

    private String value;

    private EventChannel(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }
  }

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
   * <p>
   * Example:
   * <pre>
   * {
   *   name: 'Collaborative State Machine Name',
   *   states: [...],
   *   localContext: [...],
   *   persistentContext: [...],
   *   guards: [],
   *   actions: [],
   * }
   * </pre>
   *
   * @since CSML 0.1.
   */
  public static final class StateMachineDescription extends StateOrStateMachineDescription {

    private final @NonNull String name;

    private final @NonNull List<? extends @NonNull StateOrStateMachineDescription> states;

    private final ContextDescription localContext;

    private final ContextDescription persistentContext;

    private final @NonNull List<@NonNull GuardDescription> guards;

    private final @NonNull List<? extends @NonNull ActionDescription> actions;

    public StateMachineDescription(@Named("name") @NonNull String name,
        @Named("states") @NonNull List<? extends @NonNull StateOrStateMachineDescription> states,
        @Named("localContext") ContextDescription localContext,
        @Named("persistentContext") ContextDescription persistentContext,
        @Named("guards") @NonNull List<@NonNull GuardDescription> guards,
        @Named("actions") @NonNull List<? extends @NonNull ActionDescription> actions) {
      this.name = name;
      this.states = states;
      this.localContext = localContext;
      this.persistentContext = persistentContext;
      this.guards = guards;
      this.actions = actions;
    }

    /**
     * The name.
     */
    public @NonNull String getName() {
      return name;
    }

    public StateMachineDescription withName(@NonNull String name) {
      return new StateMachineDescription(name, states, localContext, persistentContext, guards, actions);
    }

    /**
     * The states.
     * <p>
     * At least one initial state must be provided.
     * </p>
     */
    public @NonNull List<? extends @NonNull StateOrStateMachineDescription> getStates() {
      return states;
    }

    public StateMachineDescription withStates(
        @NonNull List<? extends @NonNull StateOrStateMachineDescription> states) {
      return new StateMachineDescription(name, states, localContext, persistentContext, guards, actions);
    }

    /**
     * The optional lexical declaration of local context variables.
     */
    public ContextDescription getLocalContext() {
      return localContext;
    }

    public StateMachineDescription withLocalContext(ContextDescription localContext) {
      return new StateMachineDescription(name, states, localContext, persistentContext, guards, actions);
    }

    /**
     * The optional lexical declaration of persistent context variables.
     */
    public ContextDescription getPersistentContext() {
      return persistentContext;
    }

    public StateMachineDescription withPersistentContext(ContextDescription persistentContext) {
      return new StateMachineDescription(name, states, localContext, persistentContext, guards, actions);
    }

    /**
     * The optional named guards.
     * <p>
     * The guards declared here may be used inside this state machine by referencing the names.
     * </p>
     */
    public @NonNull List<@NonNull GuardDescription> getGuards() {
      return guards;
    }

    public StateMachineDescription withGuards(@NonNull List<@NonNull GuardDescription> guards) {
      return new StateMachineDescription(name, states, localContext, persistentContext, guards, actions);
    }

    /**
     * The optional named actions.
     * <p>
     * The actions declared here may be used inside this state machine by referencing the names.
     * </p>
     */
    public @NonNull List<? extends @NonNull ActionDescription> getActions() {
      return actions;
    }

    public StateMachineDescription withActions(
        @NonNull List<? extends @NonNull ActionDescription> actions) {
      return new StateMachineDescription(name, states, localContext, persistentContext, guards, actions);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      StateMachineDescription other = (StateMachineDescription) obj;
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.states, other.states)) {
        return false;
      }
      if (!Objects.equals(this.localContext, other.localContext)) {
        return false;
      }
      if (!Objects.equals(this.persistentContext, other.persistentContext)) {
        return false;
      }
      if (!Objects.equals(this.guards, other.guards)) {
        return false;
      }
      if (!Objects.equals(this.actions, other.actions)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.states);
      result = 31 * result + Objects.hashCode(this.localContext);
      result = 31 * result + Objects.hashCode(this.persistentContext);
      result = 31 * result + Objects.hashCode(this.guards);
      result = 31 * result + Objects.hashCode(this.actions);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(350);
      builder.append(StateMachineDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "states", this.states);
      appendProperty(builder, "localContext", this.localContext);
      appendProperty(builder, "persistentContext", this.persistentContext);
      appendProperty(builder, "guards", this.guards);
      appendProperty(builder, "actions", this.actions);
      builder.append("\n}");
      return builder.toString();
    }
  }

  public abstract static class StateOrStateMachineDescription {

    protected StateOrStateMachineDescription() {
    }
  }

  /**
   * State construct, represents an atomic state of a state machine.
   * <p>
   * Keywords:
   * <table border="1">
   *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
   *  <tr><td>name</td><td>Unique name</td><td>Yes</td></tr>
   *  <tr><td>initial</td><td>Initial state flag</td><td>No</td></tr>
   *  <tr><td>terminal</td><td>Terminal state flag</td><td>No</td></tr>
   *  <tr><td>entry</td><td>On entry actions</td><td>No</td></tr>
   *  <tr><td>exit</td><td>On exit actions</td><td>No</td></tr>
   *  <tr><td>while</td><td>While actions</td><td>No</td></tr>
   *  <tr><td>after</td><td>Timeout actions</td><td>No</td></tr>
   *  <tr><td>on</td><td>On transitions</td><td>No</td></tr>
   *  <tr><td>always</td><td>Always transitions</td><td>No</td></tr>
   *  <tr><td>localContext</td><td>Lexical description of the local context</td><td>No</td></tr>
   *  <tr><td>persistentContext</td><td>Lexical description of the persistent context</td><td>No</td></tr>
   *  <tr><td>staticContext</td><td>Lexical description of the static context</td><td>No</td></tr>
   *  <tr><td>virtual</td><td>Virtual state flag</td><td>No</td></tr>
   *  <tr><td>abstract</td><td>Abstract state flag</td><td>No</td></tr>
   * </table>
   * <p>
   * Example:
   * <pre>
   * {
   *   name: 'State Name',
   *   initial: true,
   *   terminal: false,
   *   entry: [...],
   *   exit: [...],
   *   while: [...],
   *   after: [...],
   *   on: [...],
   *   localContext: [...],
   *   persistentContext: [...],
   *   staticContext: [...]
   * }
   * </pre>
   *
   * @since CSML 0.1.
   */
  public static final class StateDescription extends StateOrStateMachineDescription {

    private final @NonNull String name;

    private final boolean initial;

    private final boolean terminal;

    private final @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> entry;

    private final @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> exit;

    private final @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> _while;

    private final @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> after;

    private final @NonNull List<@NonNull OnTransitionDescription> on;

    private final @NonNull List<? extends @NonNull TransitionDescription> always;

    private final @NonNull ContextDescription localContext;

    private final @NonNull ContextDescription persistentContext;

    private final @NonNull ContextDescription staticContext;

    public StateDescription(@Named("name") @NonNull String name, @Named("initial") boolean initial,
        @Named("terminal") boolean terminal,
        @Named("entry") @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> entry,
        @Named("exit") @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> exit,
        @Named("while") @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> _while,
        @Named("after") @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> after,
        @Named("on") @NonNull List<@NonNull OnTransitionDescription> on,
        @Named("always") @NonNull List<? extends @NonNull TransitionDescription> always,
        @Named("localContext") @NonNull ContextDescription localContext,
        @Named("persistentContext") @NonNull ContextDescription persistentContext,
        @Named("staticContext") @NonNull ContextDescription staticContext) {
      this.name = name;
      this.initial = initial;
      this.terminal = terminal;
      this.entry = entry;
      this.exit = exit;
      this._while = _while;
      this.after = after;
      this.on = on;
      this.always = always;
      this.localContext = localContext;
      this.persistentContext = persistentContext;
      this.staticContext = staticContext;
    }

    /**
     * The name.
     */
    public @NonNull String getName() {
      return name;
    }

    public StateDescription withName(@NonNull String name) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The is initial flag. Indicating if this is the initial state of the state machine. Exactly one state must be the initial state of a
     * state machine. If omitted, the state is not initial.
     */
    public boolean isInitial() {
      return initial;
    }

    public StateDescription withInitial(boolean initial) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The is terminal flag. Indicating if this is a terminal state of the state machine. If omitted, the state is not terminal.
     */
    public boolean isTerminal() {
      return terminal;
    }

    public StateDescription withTerminal(boolean terminal) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The optional entry actions. Can be provided as action references to previously declared actions, or inline actions.
     */
    public @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> getEntry() {
      return entry;
    }

    public StateDescription withEntry(
        @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> entry) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The optional exit actions. Can be provided as action references to previously declared actions, or inline actions.
     */
    public @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> getExit() {
      return exit;
    }

    public StateDescription withExit(
        @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> exit) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The optional while actions. Can be provided as action references to previously declared actions, or inline actions.
     */
    public @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> getWhile() {
      return _while;
    }

    public StateDescription withWhile(
        @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> _while) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The optional after (timeout) actions. Can be provided as action references to previously declared actions, or inline actions. Actions
     * provided must be raise event actions.
     */
    public @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> getAfter() {
      return after;
    }

    public StateDescription withAfter(
        @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> after) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The optional on transitions. On transitions are taken upon event receiving an event that matches the 'event' keyword of the on
     * transition.
     */
    public @NonNull List<@NonNull OnTransitionDescription> getOn() {
      return on;
    }

    public StateDescription withOn(@NonNull List<@NonNull OnTransitionDescription> on) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The optional always transitions. Always transitions are always taken upon entering a state.
     */
    public @NonNull List<? extends @NonNull TransitionDescription> getAlways() {
      return always;
    }

    public StateDescription withAlways(
        @NonNull List<? extends @NonNull TransitionDescription> always) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The optional lexical declaration of local context variables.
     */
    public @NonNull ContextDescription getLocalContext() {
      return localContext;
    }

    public StateDescription withLocalContext(@NonNull ContextDescription localContext) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The optional lexical declaration of persistent context variables.
     */
    public @NonNull ContextDescription getPersistentContext() {
      return persistentContext;
    }

    public StateDescription withPersistentContext(@NonNull ContextDescription persistentContext) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    /**
     * The optional lexical declaration of static context variables.
     */
    public @NonNull ContextDescription getStaticContext() {
      return staticContext;
    }

    public StateDescription withStaticContext(@NonNull ContextDescription staticContext) {
      return new StateDescription(name, initial, terminal, entry, exit, _while, after, on, always, localContext, persistentContext,
          staticContext);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      StateDescription other = (StateDescription) obj;
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.initial, other.initial)) {
        return false;
      }
      if (!Objects.equals(this.terminal, other.terminal)) {
        return false;
      }
      if (!Objects.equals(this.entry, other.entry)) {
        return false;
      }
      if (!Objects.equals(this.exit, other.exit)) {
        return false;
      }
      if (!Objects.equals(this._while, other._while)) {
        return false;
      }
      if (!Objects.equals(this.after, other.after)) {
        return false;
      }
      if (!Objects.equals(this.on, other.on)) {
        return false;
      }
      if (!Objects.equals(this.always, other.always)) {
        return false;
      }
      if (!Objects.equals(this.localContext, other.localContext)) {
        return false;
      }
      if (!Objects.equals(this.persistentContext, other.persistentContext)) {
        return false;
      }
      if (!Objects.equals(this.staticContext, other.staticContext)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.initial);
      result = 31 * result + Objects.hashCode(this.terminal);
      result = 31 * result + Objects.hashCode(this.entry);
      result = 31 * result + Objects.hashCode(this.exit);
      result = 31 * result + Objects.hashCode(this._while);
      result = 31 * result + Objects.hashCode(this.after);
      result = 31 * result + Objects.hashCode(this.on);
      result = 31 * result + Objects.hashCode(this.always);
      result = 31 * result + Objects.hashCode(this.localContext);
      result = 31 * result + Objects.hashCode(this.persistentContext);
      result = 31 * result + Objects.hashCode(this.staticContext);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(650);
      builder.append(StateDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "initial", this.initial);
      appendProperty(builder, "terminal", this.terminal);
      appendProperty(builder, "entry", this.entry);
      appendProperty(builder, "exit", this.exit);
      appendProperty(builder, "_while", this._while);
      appendProperty(builder, "after", this.after);
      appendProperty(builder, "on", this.on);
      appendProperty(builder, "always", this.always);
      appendProperty(builder, "localContext", this.localContext);
      appendProperty(builder, "persistentContext", this.persistentContext);
      appendProperty(builder, "staticContext", this.staticContext);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Transition construct. Represents a transition that is to be taken regardless of an event.
   * <p>
   * Keywords:
   * <table border="1">
   *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
   *  <tr><td>target</td><td>Target state</td><td>Yes</td></tr>
   *  <tr><td>guards</td><td>Guards</td><td>Yes</td></tr>
   *  <tr><td>actions</td><td>Actions</td><td>Yes</td></tr>
   *  <tr><td>else</td><td>Else target</td><td>No</td></tr>
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
  public static class TransitionDescription {

    protected final String target;

    protected final @NonNull List<? extends @NonNull GuardOrGuardReferenceDescription> guards;

    protected final @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> actions;

    protected final String _else;

    public TransitionDescription(@Named("target") String target,
        @Named("guards") @NonNull List<? extends @NonNull GuardOrGuardReferenceDescription> guards,
        @Named("actions") @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> actions,
        @Named("else") String _else) {
      this.target = target;
      this.guards = guards;
      this.actions = actions;
      this._else = _else;
    }

    /**
     * The optional target state name. If the target is omitted, the transition is internal.
     */
    public String getTarget() {
      return target;
    }

    public TransitionDescription withTarget(String target) {
      return new TransitionDescription(target, guards, actions, _else);
    }

    /**
     * The optional guards. All guard expressions need to evaluate to true before a transition can be taken. Can be provided as guard
     * references to previously declared guards, or inline guards.
     */
    public @NonNull List<? extends @NonNull GuardOrGuardReferenceDescription> getGuards() {
      return guards;
    }

    public TransitionDescription withGuards(
        @NonNull List<? extends @NonNull GuardOrGuardReferenceDescription> guards) {
      return new TransitionDescription(target, guards, actions, _else);
    }

    /**
     * The optional actions. These actions are executed during the transition, if the transition is taken. Can be provided as action
     * references to previously declared actions, or inline actions.
     */
    public @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> getActions() {
      return actions;
    }

    public TransitionDescription withActions(
        @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> actions) {
      return new TransitionDescription(target, guards, actions, _else);
    }

    /**
     * The optional else target name. If the guards evaluate to false, the state machine ends up in this target state.
     */
    public String getElse() {
      return _else;
    }

    public TransitionDescription withElse(String _else) {
      return new TransitionDescription(target, guards, actions, _else);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      TransitionDescription other = (TransitionDescription) obj;
      if (!Objects.equals(this.target, other.target)) {
        return false;
      }
      if (!Objects.equals(this.guards, other.guards)) {
        return false;
      }
      if (!Objects.equals(this.actions, other.actions)) {
        return false;
      }
      if (!Objects.equals(this._else, other._else)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.target);
      result = 31 * result + Objects.hashCode(this.guards);
      result = 31 * result + Objects.hashCode(this.actions);
      result = 31 * result + Objects.hashCode(this._else);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(250);
      builder.append(TransitionDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "target", this.target);
      appendProperty(builder, "guards", this.guards);
      appendProperty(builder, "actions", this.actions);
      appendProperty(builder, "_else", this._else);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * [deprecated]
   */
  public abstract static class GuardOrGuardReferenceDescription {

    protected GuardOrGuardReferenceDescription() {
    }
  }

  /**
   * Guard construct. Represents a conditional (if) that determines if a transition can be taken. Guards can be declared and referenced as
   * part of a state machine, or be declared inline.
   * <p>
   * Keywords:
   * <table border="1">
   *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
   *  <tr><td>name</td><td>Unique name</td><td>Yes</td></tr>
   *  <tr><td>expression</td><td>Expression</td><td>Yes</td></tr>
   * </table>
   * <p>
   * Example:
   * <pre>
   * {
   *   name: 'Guard Name',
   *   expression: 'a==5'
   * }
   * </pre>
   *
   * @since CSML 0.1.
   */
  public static final class GuardDescription extends GuardOrGuardReferenceDescription {

    private final String name;

    private final @NonNull String expression;

    public GuardDescription(@Named("name") String name,
        @Named("expression") @NonNull String expression) {
      this.name = name;
      this.expression = expression;
    }

    /**
     * The optional name.
     * <p>
     * If present, can be referenced from within a state machine component when declared as part of the state machine's guards.
     */
    public String getName() {
      return name;
    }

    public GuardDescription withName(String name) {
      return new GuardDescription(name, expression);
    }

    /**
     * An expression.
     * <p>
     * The expression must evaluate to a boolean value.
     * </p>
     */
    public @NonNull String getExpression() {
      return expression;
    }

    public GuardDescription withExpression(@NonNull String expression) {
      return new GuardDescription(name, expression);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      GuardDescription other = (GuardDescription) obj;
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.expression, other.expression)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.expression);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(150);
      builder.append(GuardDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "expression", this.expression);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * [deprecated]
   */
  public static final class GuardReferenceDescription extends GuardOrGuardReferenceDescription {

    private final @NonNull String reference;

    public GuardReferenceDescription(@Named("reference") @NonNull String reference) {
      this.reference = reference;
    }

    public @NonNull String getReference() {
      return reference;
    }

    public GuardReferenceDescription withReference(@NonNull String reference) {
      return new GuardReferenceDescription(reference);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      GuardReferenceDescription other = (GuardReferenceDescription) obj;
      if (!Objects.equals(this.reference, other.reference)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.reference);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(100);
      builder.append(GuardReferenceDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "reference", this.reference);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * On transition construct. Represents a transition that is to be taken based on a received event.
   * <p>
   * Keywords:
   * <table border="1">
   *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
   *  <tr><td>event</td><td>Event</td><td>Yes</td></tr>
   * </table>
   * <p>
   * Example:
   * <pre>
   * {
   *   target: 'StateClass Name',
   *   guards: [...],
   *   actions: [...],
   *   event: 'Event Name'
   * }
   * </pre>
   *
   * @since CSML 0.1.
   */
  public static final class OnTransitionDescription extends TransitionDescription {

    private final @NonNull String event;

    public OnTransitionDescription(@Named("target") String target,
        @Named("guards") @NonNull List<? extends @NonNull GuardOrGuardReferenceDescription> guards,
        @Named("actions") @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> actions,
        @Named("else") String _else, @Named("event") @NonNull String event) {
      super(target, guards, actions, _else);
      this.event = event;
    }

    public OnTransitionDescription withTarget(String target) {
      return new OnTransitionDescription(target, guards, actions, _else, event);
    }

    public OnTransitionDescription withGuards(
        @NonNull List<? extends @NonNull GuardOrGuardReferenceDescription> guards) {
      return new OnTransitionDescription(target, guards, actions, _else, event);
    }

    public OnTransitionDescription withActions(
        @NonNull List<? extends @NonNull ActionOrActionReferenceDescription> actions) {
      return new OnTransitionDescription(target, guards, actions, _else, event);
    }

    public OnTransitionDescription withElse(String _else) {
      return new OnTransitionDescription(target, guards, actions, _else, event);
    }

    public @NonNull String getEvent() {
      return event;
    }

    public OnTransitionDescription withEvent(@NonNull String event) {
      return new OnTransitionDescription(target, guards, actions, _else, event);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      OnTransitionDescription other = (OnTransitionDescription) obj;
      if (!Objects.equals(this.target, other.target)) {
        return false;
      }
      if (!Objects.equals(this.guards, other.guards)) {
        return false;
      }
      if (!Objects.equals(this.actions, other.actions)) {
        return false;
      }
      if (!Objects.equals(this._else, other._else)) {
        return false;
      }
      if (!Objects.equals(this.event, other.event)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.target);
      result = 31 * result + Objects.hashCode(this.guards);
      result = 31 * result + Objects.hashCode(this.actions);
      result = 31 * result + Objects.hashCode(this._else);
      result = 31 * result + Objects.hashCode(this.event);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(300);
      builder.append(OnTransitionDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "target", this.target);
      appendProperty(builder, "guards", this.guards);
      appendProperty(builder, "actions", this.actions);
      appendProperty(builder, "_else", this._else);
      appendProperty(builder, "event", this.event);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * [deprecated]
   */
  public abstract static class ActionOrActionReferenceDescription {

    protected ActionOrActionReferenceDescription() {
    }
  }

  /**
   * An abstract action construct. Represents an action that can be taken in a state machine.
   */
  public abstract static class ActionDescription extends ActionOrActionReferenceDescription {

    protected final @NonNull Type type;

    protected final String name;

    protected ActionDescription(@Named("type") @NonNull Type type, @Named("name") String name) {
      this.type = type;
      this.name = name;
    }

    public @NonNull Type getType() {
      return type;
    }

    public String getName() {
      return name;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      ActionDescription other = (ActionDescription) obj;
      if (!Objects.equals(this.type, other.type)) {
        return false;
      }
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.name);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(150);
      builder.append(ActionDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "type", this.type);
      appendProperty(builder, "name", this.name);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * [deprecated]
   */
  public static final class ActionReferenceDescription extends ActionOrActionReferenceDescription {

    private final @NonNull String reference;

    public ActionReferenceDescription(@Named("reference") @NonNull String reference) {
      this.reference = reference;
    }

    public @NonNull String getReference() {
      return reference;
    }

    public ActionReferenceDescription withReference(@NonNull String reference) {
      return new ActionReferenceDescription(reference);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      ActionReferenceDescription other = (ActionReferenceDescription) obj;
      if (!Objects.equals(this.reference, other.reference)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.reference);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(100);
      builder.append(ActionReferenceDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "reference", this.reference);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Assign action construct. Represents an assignment of a value to a context variable.
   */
  public static final class AssignActionDescription extends ActionDescription {

    private final @NonNull Type type;

    private final @NonNull ContextVariableDescription variable;

    public AssignActionDescription(@Named("name") String name, @Named("type") @NonNull Type type,
        @Named("variable") @NonNull ContextVariableDescription variable) {
      super(type, name);
      this.type = type;
      this.variable = variable;
    }

    @Override
    public @NonNull Type getType() {
      return type;
    }

    public AssignActionDescription withType(@NonNull Type type) {
      return new AssignActionDescription(name, type, variable);
    }

    public AssignActionDescription withName(String name) {
      return new AssignActionDescription(name, type, variable);
    }

    public @NonNull ContextVariableDescription getVariable() {
      return variable;
    }

    public AssignActionDescription withVariable(@NonNull ContextVariableDescription variable) {
      return new AssignActionDescription(name, type, variable);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      AssignActionDescription other = (AssignActionDescription) obj;
      if (!Objects.equals(this.type, other.type)) {
        return false;
      }
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.variable, other.variable)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.variable);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(200);
      builder.append(AssignActionDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "type", this.type);
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "variable", this.variable);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Create action construct. Represents the creation of a context variable.
   */
  public static final class CreateActionDescription extends ActionDescription {

    private final @NonNull Type type;

    private final @NonNull ContextVariableDescription variable;

    private final boolean isPersistent;

    public CreateActionDescription(@Named("name") String name, @Named("type") @NonNull Type type,
        @Named("variable") @NonNull ContextVariableDescription variable,
        @Named("isPersistent") boolean isPersistent) {
      super(type, name);
      this.type = type;
      this.variable = variable;
      this.isPersistent = isPersistent;
    }

    @Override
    public @NonNull Type getType() {
      return type;
    }

    public CreateActionDescription withType(@NonNull Type type) {
      return new CreateActionDescription(name, type, variable, isPersistent);
    }

    public CreateActionDescription withName(String name) {
      return new CreateActionDescription(name, type, variable, isPersistent);
    }

    /**
     * The context variable to be created.
     */
    public @NonNull ContextVariableDescription getVariable() {
      return variable;
    }

    public CreateActionDescription withVariable(@NonNull ContextVariableDescription variable) {
      return new CreateActionDescription(name, type, variable, isPersistent);
    }

    /**
     * Determines if the context variable is persistent.
     */
    public boolean isIsPersistent() {
      return isPersistent;
    }

    public CreateActionDescription withIsPersistent(boolean isPersistent) {
      return new CreateActionDescription(name, type, variable, isPersistent);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      CreateActionDescription other = (CreateActionDescription) obj;
      if (!Objects.equals(this.type, other.type)) {
        return false;
      }
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.variable, other.variable)) {
        return false;
      }
      if (!Objects.equals(this.isPersistent, other.isPersistent)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.variable);
      result = 31 * result + Objects.hashCode(this.isPersistent);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(250);
      builder.append(CreateActionDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "type", this.type);
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "variable", this.variable);
      appendProperty(builder, "isPersistent", this.isPersistent);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Invoke action construct. Represents the invocation of a service.
   */
  public static final class InvokeActionDescription extends ActionDescription {

    private final @NonNull Type type;

    private final @NonNull String serviceType;

    private final boolean isLocal;

    private final @NonNull List<@NonNull ContextVariableDescription> input;

    private final @NonNull List<@NonNull EventDescription> done;

    private final @NonNull List<@NonNull ContextVariableReferenceDescription> output;

    public InvokeActionDescription(@Named("name") String name, @Named("type") @NonNull Type type,
        @Named("serviceType") @NonNull String serviceType, @Named("isLocal") boolean isLocal,
        @Named("input") @NonNull List<@NonNull ContextVariableDescription> input,
        @Named("done") @NonNull List<@NonNull EventDescription> done,
        @Named("output") @NonNull List<@NonNull ContextVariableReferenceDescription> output) {
      super(type, name);
      this.type = type;
      this.serviceType = serviceType;
      this.isLocal = isLocal;
      this.input = input;
      this.done = done;
      this.output = output;
    }

    @Override
    public @NonNull Type getType() {
      return type;
    }

    public InvokeActionDescription withType(@NonNull Type type) {
      return new InvokeActionDescription(name, type, serviceType, isLocal, input, done, output);
    }

    public InvokeActionDescription withName(String name) {
      return new InvokeActionDescription(name, type, serviceType, isLocal, input, done, output);
    }

    /**
     * The service type.
     */
    public @NonNull String getServiceType() {
      return serviceType;
    }

    public InvokeActionDescription withServiceType(@NonNull String serviceType) {
      return new InvokeActionDescription(name, type, serviceType, isLocal, input, done, output);
    }

    /**
     * Determines if the service is local.
     */
    public boolean isIsLocal() {
      return isLocal;
    }

    public InvokeActionDescription withIsLocal(boolean isLocal) {
      return new InvokeActionDescription(name, type, serviceType, isLocal, input, done, output);
    }

    /**
     * The input parameters.
     */
    public @NonNull List<@NonNull ContextVariableDescription> getInput() {
      return input;
    }

    public InvokeActionDescription withInput(
        @NonNull List<@NonNull ContextVariableDescription> input) {
      return new InvokeActionDescription(name, type, serviceType, isLocal, input, done, output);
    }

    /**
     * The events to be raised when the service is done.
     */
    public @NonNull List<@NonNull EventDescription> getDone() {
      return done;
    }

    public InvokeActionDescription withDone(@NonNull List<@NonNull EventDescription> done) {
      return new InvokeActionDescription(name, type, serviceType, isLocal, input, done, output);
    }

    /**
     * The output mappings to context variables.
     */
    public @NonNull List<@NonNull ContextVariableReferenceDescription> getOutput() {
      return output;
    }

    public InvokeActionDescription withOutput(
        @NonNull List<@NonNull ContextVariableReferenceDescription> output) {
      return new InvokeActionDescription(name, type, serviceType, isLocal, input, done, output);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      InvokeActionDescription other = (InvokeActionDescription) obj;
      if (!Objects.equals(this.type, other.type)) {
        return false;
      }
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.serviceType, other.serviceType)) {
        return false;
      }
      if (!Objects.equals(this.isLocal, other.isLocal)) {
        return false;
      }
      if (!Objects.equals(this.input, other.input)) {
        return false;
      }
      if (!Objects.equals(this.done, other.done)) {
        return false;
      }
      if (!Objects.equals(this.output, other.output)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.serviceType);
      result = 31 * result + Objects.hashCode(this.isLocal);
      result = 31 * result + Objects.hashCode(this.input);
      result = 31 * result + Objects.hashCode(this.done);
      result = 31 * result + Objects.hashCode(this.output);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(400);
      builder.append(InvokeActionDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "type", this.type);
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "serviceType", this.serviceType);
      appendProperty(builder, "isLocal", this.isLocal);
      appendProperty(builder, "input", this.input);
      appendProperty(builder, "done", this.done);
      appendProperty(builder, "output", this.output);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Match action construct. Represents a match action.
   */
  public static final class MatchActionDescription extends ActionDescription {

    private final @NonNull Type type;

    private final @NonNull String value;

    private final @NonNull List<@NonNull MatchCaseDescription> cases;

    public MatchActionDescription(@Named("name") String name, @Named("type") @NonNull Type type,
        @Named("value") @NonNull String value,
        @Named("cases") @NonNull List<@NonNull MatchCaseDescription> cases) {
      super(type, name);
      this.type = type;
      this.value = value;
      this.cases = cases;
    }

    @Override
    public @NonNull Type getType() {
      return type;
    }

    public MatchActionDescription withType(@NonNull Type type) {
      return new MatchActionDescription(name, type, value, cases);
    }

    public MatchActionDescription withName(String name) {
      return new MatchActionDescription(name, type, value, cases);
    }

    /**
     * The value to be matched.
     */
    public @NonNull String getValue() {
      return value;
    }

    public MatchActionDescription withValue(@NonNull String value) {
      return new MatchActionDescription(name, type, value, cases);
    }

    /**
     * The match cases.
     */
    public @NonNull List<@NonNull MatchCaseDescription> getCases() {
      return cases;
    }

    public MatchActionDescription withCases(@NonNull List<@NonNull MatchCaseDescription> cases) {
      return new MatchActionDescription(name, type, value, cases);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      MatchActionDescription other = (MatchActionDescription) obj;
      if (!Objects.equals(this.type, other.type)) {
        return false;
      }
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.value, other.value)) {
        return false;
      }
      if (!Objects.equals(this.cases, other.cases)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.value);
      result = 31 * result + Objects.hashCode(this.cases);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(250);
      builder.append(MatchActionDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "type", this.type);
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "value", this.value);
      appendProperty(builder, "cases", this.cases);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Match case construct.
   */
  public static final class MatchCaseDescription {

    private final @NonNull String _case;

    private final @NonNull ActionOrActionReferenceDescription action;

    public MatchCaseDescription(@Named("case") @NonNull String _case,
        @Named("action") @NonNull ActionOrActionReferenceDescription action) {
      this._case = _case;
      this.action = action;
    }

    /**
     * The case value.
     */
    public @NonNull String getCase() {
      return _case;
    }

    public MatchCaseDescription withCase(@NonNull String _case) {
      return new MatchCaseDescription(_case, action);
    }

    /**
     * The actions to be taken if the case value matches the value of the match action.
     */
    public @NonNull ActionOrActionReferenceDescription getAction() {
      return action;
    }

    public MatchCaseDescription withAction(@NonNull ActionOrActionReferenceDescription action) {
      return new MatchCaseDescription(_case, action);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      MatchCaseDescription other = (MatchCaseDescription) obj;
      if (!Objects.equals(this._case, other._case)) {
        return false;
      }
      if (!Objects.equals(this.action, other.action)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this._case);
      result = 31 * result + Objects.hashCode(this.action);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(150);
      builder.append(MatchCaseDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "_case", this._case);
      appendProperty(builder, "action", this.action);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Raise action construct. Represents the raising of an event.
   */
  public static final class RaiseActionDescription extends ActionDescription {

    private final @NonNull Type type;

    private final @NonNull EventDescription event;

    public RaiseActionDescription(@Named("name") String name, @Named("type") @NonNull Type type,
        @Named("event") @NonNull EventDescription event) {
      super(type, name);
      this.type = type;
      this.event = event;
    }

    @Override
    public @NonNull Type getType() {
      return type;
    }

    public RaiseActionDescription withType(@NonNull Type type) {
      return new RaiseActionDescription(name, type, event);
    }

    public RaiseActionDescription withName(String name) {
      return new RaiseActionDescription(name, type, event);
    }

    /**
     * The event to be raised.
     */
    public @NonNull EventDescription getEvent() {
      return event;
    }

    public RaiseActionDescription withEvent(@NonNull EventDescription event) {
      return new RaiseActionDescription(name, type, event);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      RaiseActionDescription other = (RaiseActionDescription) obj;
      if (!Objects.equals(this.type, other.type)) {
        return false;
      }
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.event, other.event)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.event);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(200);
      builder.append(RaiseActionDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "type", this.type);
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "event", this.event);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Timeout action construct. Represents a timeout action.
   */
  public static final class TimeoutActionDescription extends ActionDescription {

    private final @NonNull Type type;

    private final @NonNull String delay;

    private final @NonNull ActionOrActionReferenceDescription action;

    public TimeoutActionDescription(@Named("name") String name, @Named("type") @NonNull Type type,
        @Named("delay") @NonNull String delay,
        @Named("action") @NonNull ActionOrActionReferenceDescription action) {
      super(type, name);
      this.type = type;
      this.delay = delay;
      this.action = action;
    }

    @Override
    public @NonNull Type getType() {
      return type;
    }

    public TimeoutActionDescription withType(@NonNull Type type) {
      return new TimeoutActionDescription(name, type, delay, action);
    }

    public TimeoutActionDescription withName(String name) {
      return new TimeoutActionDescription(name, type, delay, action);
    }

    /**
     * The delay until the timeout action is executed.
     */
    public @NonNull String getDelay() {
      return delay;
    }

    public TimeoutActionDescription withDelay(@NonNull String delay) {
      return new TimeoutActionDescription(name, type, delay, action);
    }

    /**
     * The action to be executed when the timeout occurs.
     */
    public @NonNull ActionOrActionReferenceDescription getAction() {
      return action;
    }

    public TimeoutActionDescription withAction(@NonNull ActionOrActionReferenceDescription action) {
      return new TimeoutActionDescription(name, type, delay, action);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      TimeoutActionDescription other = (TimeoutActionDescription) obj;
      if (!Objects.equals(this.type, other.type)) {
        return false;
      }
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.delay, other.delay)) {
        return false;
      }
      if (!Objects.equals(this.action, other.action)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.delay);
      result = 31 * result + Objects.hashCode(this.action);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(250);
      builder.append(TimeoutActionDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "type", this.type);
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "delay", this.delay);
      appendProperty(builder, "action", this.action);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Timeout reset action construct. Represents a timeout reset action.
   */
  public static final class TimeoutResetActionDescription extends ActionDescription {

    private final @NonNull Type type;

    private final @NonNull String action;

    public TimeoutResetActionDescription(@Named("name") String name,
        @Named("type") @NonNull Type type, @Named("action") @NonNull String action) {
      super(type, name);
      this.type = type;
      this.action = action;
    }

    @Override
    public @NonNull Type getType() {
      return type;
    }

    public TimeoutResetActionDescription withType(@NonNull Type type) {
      return new TimeoutResetActionDescription(name, type, action);
    }

    public TimeoutResetActionDescription withName(String name) {
      return new TimeoutResetActionDescription(name, type, action);
    }

    /**
     * The timeout action to reset.
     */
    public @NonNull String getAction() {
      return action;
    }

    public TimeoutResetActionDescription withAction(@NonNull String action) {
      return new TimeoutResetActionDescription(name, type, action);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      TimeoutResetActionDescription other = (TimeoutResetActionDescription) obj;
      if (!Objects.equals(this.type, other.type)) {
        return false;
      }
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.action, other.action)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.action);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(200);
      builder.append(TimeoutResetActionDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "type", this.type);
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "action", this.action);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Event construct. Represents an event that can be raised.
   */
  public static final class EventDescription {

    private final @NonNull String name;

    private final @NonNull EventChannel channel;

    private final @NonNull List<@NonNull ContextVariableDescription> data;

    public EventDescription(@Named("name") @NonNull String name,
        @Named("channel") @NonNull EventChannel channel,
        @Named("data") @NonNull List<@NonNull ContextVariableDescription> data) {
      this.name = name;
      this.channel = channel;
      this.data = data;
    }

    /**
     * The event name.
     */
    public @NonNull String getName() {
      return name;
    }

    public EventDescription withName(@NonNull String name) {
      return new EventDescription(name, channel, data);
    }

    /**
     * The event channel.
     */
    public @NonNull EventChannel getChannel() {
      return channel;
    }

    public EventDescription withChannel(@NonNull EventChannel channel) {
      return new EventDescription(name, channel, data);
    }

    /**
     * The event data.
     */
    public @NonNull List<@NonNull ContextVariableDescription> getData() {
      return data;
    }

    public EventDescription withData(@NonNull List<@NonNull ContextVariableDescription> data) {
      return new EventDescription(name, channel, data);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      EventDescription other = (EventDescription) obj;
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.channel, other.channel)) {
        return false;
      }
      if (!Objects.equals(this.data, other.data)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.channel);
      result = 31 * result + Objects.hashCode(this.data);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(200);
      builder.append(EventDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "channel", this.channel);
      appendProperty(builder, "data", this.data);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Context variable reference construct. Represents a reference to a context variable.
   */
  public static final class ContextVariableReferenceDescription {

    private final @NonNull String reference;

    public ContextVariableReferenceDescription(@Named("reference") @NonNull String reference) {
      this.reference = reference;
    }

    public @NonNull String getReference() {
      return reference;
    }

    public ContextVariableReferenceDescription withReference(@NonNull String reference) {
      return new ContextVariableReferenceDescription(reference);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      ContextVariableReferenceDescription other = (ContextVariableReferenceDescription) obj;
      if (!Objects.equals(this.reference, other.reference)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.reference);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(100);
      builder.append(ContextVariableReferenceDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "reference", this.reference);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Context description construct. Represents a context.
   */
  public static final class ContextDescription {

    private final @NonNull List<@NonNull ContextVariableDescription> variables;

    public ContextDescription(
        @Named("variables") @NonNull List<@NonNull ContextVariableDescription> variables) {
      this.variables = variables;
    }

    public @NonNull List<@NonNull ContextVariableDescription> getVariables() {
      return variables;
    }

    public ContextDescription withVariables(
        @NonNull List<@NonNull ContextVariableDescription> variables) {
      return new ContextDescription(variables);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      ContextDescription other = (ContextDescription) obj;
      if (!Objects.equals(this.variables, other.variables)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.variables);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(100);
      builder.append(ContextDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "variables", this.variables);
      builder.append("\n}");
      return builder.toString();
    }
  }

  /**
   * Context variable description construct. Represents a context variable.
   */
  public static final class ContextVariableDescription {

    private final @NonNull String name;

    private final @NonNull String value;

    public ContextVariableDescription(@Named("name") @NonNull String name,
        @Named("value") @NonNull String value) {
      this.name = name;
      this.value = value;
    }

    public @NonNull String getName() {
      return name;
    }

    public ContextVariableDescription withName(@NonNull String name) {
      return new ContextVariableDescription(name, value);
    }

    public @NonNull String getValue() {
      return value;
    }

    public ContextVariableDescription withValue(@NonNull String value) {
      return new ContextVariableDescription(name, value);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      ContextVariableDescription other = (ContextVariableDescription) obj;
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (!Objects.equals(this.value, other.value)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.value);
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(150);
      builder.append(ContextVariableDescription.class.getSimpleName()).append(" {");
      appendProperty(builder, "name", this.name);
      appendProperty(builder, "value", this.value);
      builder.append("\n}");
      return builder.toString();
    }
  }
}
