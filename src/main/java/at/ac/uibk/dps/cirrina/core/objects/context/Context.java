package at.ac.uibk.dps.cirrina.core.objects.context;

import at.ac.uibk.dps.cirrina.core.CoreException;

import java.util.List;

/**
 * Base context, containing context variables.
 * <p>
 * TODO: Make thread-safe.
 */
public abstract class Context {

    /**
     * Retrieve a context variable.
     *
     * @param name Name of the context variable.
     * @return The retrieved context variable.
     * @throws CoreException If the context variable could not be retrieved.
     */
    public abstract ContextVariable get(String name) throws CoreException;

    /**
     * Creates a context variable.
     *
     * @param name  Name of the context variable.
     * @param value Value of the context variable.
     * @return The created context variable.
     * @throws CoreException If the variable could not be created.
     */
    public abstract ContextVariable create(String name, Object value) throws CoreException;

    /**
     * Synchronize a variable in the context.
     *
     * @param variable Variable to synchronize.
     * @throws CoreException In case synchronization fails.
     */
    protected abstract void sync(ContextVariable variable) throws CoreException;

    /**
     * Returns all context variables.
     *
     * @return Context variables.
     */
    public abstract List<ContextVariable> getAll();

    /**
     * Context variables, contained within a context.
     */
    public static class ContextVariable {

        /**
         * The variable name.
         */
        public final String name;
        private final Context parent;
        private Object value;
        private boolean isLocked;

        /**
         * Initializes the context variable.
         *
         * @param name   Name of the context variable.
         * @param value  Current value.
         * @param parent Parent context.
         */
        ContextVariable(String name, Object value, Context parent) {
            this.name = name;
            this.value = value;
            this.parent = parent;
            this.isLocked = false;
        }

        /**
         * Returns the locked state of this variable.
         *
         * @return Locked state, true if locked, otherwise false.
         */
        public boolean isLocked() {
            return isLocked;
        }

        /**
         * Returns the current value.
         *
         * @return Current value.
         */
        public Object getValue() {
            return value;
        }

        /**
         * Sets a new value.
         *
         * @param value The new value.
         * @throws CoreException When a new value could not be assigned.
         */
        public void setValue(Object value) throws CoreException {
            this.value = value;

            try {
                parent.sync(this);
            } catch (CoreException e) {
                throw new CoreException(String.format("Failed to assign to variable: %s", e.getCause()));
            }
        }

        /**
         * Locks this variable. A variable is required to be unlocked before it can be locked.
         *
         * @throws CoreException When the variable could not be locked.
         */
        public void lock() throws CoreException {
            // We disallow locking twice (without unlocking)
            if (isLocked) {
                throw new CoreException(String.format("Attempted to lock variable '%s' twice", name));
            }
            isLocked = true;

            try {
                parent.sync(this);
            } catch (CoreException e) {
                throw new CoreException(String.format("Failed to lock variable '%s': %s", name, e.getCause()));
            }
        }

        /**
         * Unlocks this variable. A variable is required to be locked before it can be unlocked.
         *
         * @throws CoreException When the variable could not be unlocked.
         */
        public void unlock() throws CoreException {
            // We disallow unlocking twice (without locking)
            if (!isLocked) {
                throw new CoreException(String.format("Attempted to unlock variable '%s' twice", name));
            }
            isLocked = false;

            try {
                parent.sync(this);
            } catch (CoreException e) {
                throw new CoreException(String.format("Failed to unlock variable: %s", e.getCause()));
            }
        }
    }
}
