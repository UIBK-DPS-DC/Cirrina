package at.ac.uibk.dps.cirrina.core.object.context;

public class InContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return new InMemoryContext();
  }
}
