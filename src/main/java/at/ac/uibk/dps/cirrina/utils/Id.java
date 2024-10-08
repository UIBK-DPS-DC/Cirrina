package at.ac.uibk.dps.cirrina.utils;

import java.util.Objects;
import java.util.UUID;

public final class Id {

  private final UUID uuid = UUID.randomUUID();

  @Override
  public String toString() {
    return uuid.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var instanceId = (Id) o;
    return Objects.equals(uuid, instanceId.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid);
  }
}
