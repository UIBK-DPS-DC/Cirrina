package at.ac.uibk.dps.cirrina.execution.object.statemachine;

import java.util.Objects;
import java.util.UUID;

public final class StateMachineId {

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
    var instanceId = (StateMachineId) o;
    return Objects.equals(uuid, instanceId.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid);
  }
}
