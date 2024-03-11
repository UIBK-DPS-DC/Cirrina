package at.ac.uibk.dps.cirrina.core.objects.context;

public class InContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return new InMemoryContext();
  }
}
