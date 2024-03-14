package at.ac.uibk.dps.cirrina.core.runtime.context;

public class InContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return new InMemoryContext();
  }
}
