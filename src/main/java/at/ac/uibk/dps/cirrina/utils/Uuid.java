package at.ac.uibk.dps.cirrina.utils;

import java.util.Random;
import java.util.UUID;

public class Uuid {

  public static UUID insecureUuid() {
    Random r = new Random();
    return new UUID(r.nextLong(), r.nextLong());
  }
}
