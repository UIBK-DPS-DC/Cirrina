package at.ac.uibk.dps.cirrina.object.context;

public class InContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return new InMemoryContext();
  }
}
