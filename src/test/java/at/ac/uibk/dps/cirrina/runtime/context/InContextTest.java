package at.ac.uibk.dps.cirrina.runtime.context;

public class InContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return new InMemoryContext();
  }
}
