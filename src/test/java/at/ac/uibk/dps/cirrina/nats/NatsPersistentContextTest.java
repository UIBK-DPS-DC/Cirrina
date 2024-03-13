package at.ac.uibk.dps.cirrina.nats;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.ContextTest;
import org.junit.jupiter.api.Assumptions;

public class NatsPersistentContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    String natsServerURL = System.getenv("NATS_SERVER_URL");

    Assumptions.assumeFalse(natsServerURL == null, "Skipping NATS persistent context test");

    return assertDoesNotThrow(() -> new NatsPersistentContext(natsServerURL, "persistent"));
  }
}
