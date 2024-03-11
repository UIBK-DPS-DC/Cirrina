package at.ac.uibk.dps.cirrina.core.objects.context;

import at.ac.uibk.dps.cirrina.core.CoreException;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base context, containing context variables.
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
        private AtomicReference<Object> value = new AtomicReference<>();

        /**
         * Initializes the context variable.
         *
         * @param name   Name of the context variable.
         * @param value  Current value.
         * @param parent Parent context.
         */
        ContextVariable(String name, Object value, Context parent) throws CoreException {
            this.name = name;
            this.parent = parent;

            setValue(value);
        }

        /**
         * Returns the current value.
         *
         * @return Current value.
         */
        public Object getValue() {
            return value.get();
        }

        /**
         * Sets a new value.
         *
         * @param value The new value.
         * @throws CoreException When a new value could not be assigned.
         */
        public void setValue(Object value) throws CoreException {
            // Update the value
            this.value.set(value);

            // Synchronize in context
            try {
                parent.sync(this);
            } catch (CoreException e) {
                throw new CoreException(String.format("Failed to assign to variable: %s", e.getCause()));
            }
        }
    }
}
