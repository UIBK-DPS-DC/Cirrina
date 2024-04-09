package at.ac.uibk.dps.cirrina.core.object.context;

public class InMemoryContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return new InMemoryContext();
  }
}
