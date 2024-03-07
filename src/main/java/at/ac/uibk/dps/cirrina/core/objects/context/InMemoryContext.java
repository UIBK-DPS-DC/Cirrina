package at.ac.uibk.dps.cirrina.core.objects.context;

import at.ac.uibk.dps.cirrina.core.CoreException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryContext extends Context {

  private Map<String, ContextVariable> variables;

  public InMemoryContext() {
    variables = new HashMap<>();
  }

  @Override
  public ContextVariable get(String name) throws CoreException {
    if (!variables.containsKey(name)) {
      throw new CoreException(String.format("A variable with the name '%s' does not exist", name));
    }

    return variables.get(name);
  }

  @Override
  public ContextVariable create(String name, Object value) throws CoreException {
    if (variables.containsKey(name)) {
      throw new CoreException(String.format("A variable with the name '%s' already exists", name));
    }

    return variables.put(name, new ContextVariable(name, value, this));
  }

  @Override
  public List<ContextVariable> getAll() {
    return Collections.unmodifiableList(variables.values().stream().toList());
  }
}
