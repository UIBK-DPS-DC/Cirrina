package at.ac.uibk.dps.cirrina.core.objects.context;

import at.ac.uibk.dps.cirrina.core.CoreException;
import java.util.List;

public abstract class Context {

  public abstract ContextVariable get(String name) throws CoreException;

  public abstract ContextVariable create(String name, Object value) throws CoreException;

  public abstract List<ContextVariable> getAll();

  public class ContextVariable {

    public final String name;
    private Object value;
    private Context parent;

    ContextVariable(String name, Object value, Context parent) {
      this.name = name;
      this.value = value;
      this.parent = parent;
    }

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }
  }
}
