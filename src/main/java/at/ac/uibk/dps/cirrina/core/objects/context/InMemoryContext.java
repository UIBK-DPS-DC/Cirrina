package at.ac.uibk.dps.cirrina.core.objects.context;

import at.ac.uibk.dps.cirrina.core.CoreException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An in-memory context, where context variables are contained in a hash map.
 */
public class InMemoryContext extends Context {

    protected final Map<String, ContextVariable> variables;

    /**
     * Initializes an empty in-memory context.
     */
    public InMemoryContext() {
        variables = new HashMap<>();
    }

    /**
     * Retrieve a context variable.
     *
     * @param name Name of the context variable.
     * @return The retrieved context variable.
     * @throws CoreException If the context variable could not be retrieved.
     */
    @Override
    public ContextVariable get(String name) throws CoreException {
        if (!variables.containsKey(name)) {
            throw new CoreException(String.format("A variable with the name '%s' does not exist", name));
        }

        return variables.get(name);
    }

    /**
     * Creates a context variable.
     *
     * @param name  Name of the context variable.
     * @param value Value of the context variable.
     * @return The created context variable.
     * @throws CoreException If the variable could not be created.
     */
    @Override
    public ContextVariable create(String name, Object value) throws CoreException {
        if (variables.containsKey(name)) {
            throw new CoreException(String.format("A variable with the name '%s' already exists", name));
        }

        return variables.put(name, new ContextVariable(name, value, this));
    }

    /**
     * Synchronize a variable in the context.
     *
     * @param variable Variable to synchronize.
     * @throws CoreException In case synchronization fails.
     */
    @Override
    protected void sync(ContextVariable variable) throws CoreException {
    }

    /**
     * Returns all context variables.
     *
     * @return Context variables.
     */
    @Override
    public List<ContextVariable> getAll() {
        return Collections.unmodifiableList(variables.values().stream().toList());
    }
}
