package at.ac.uibk.dps.cirrina.csml.description;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.Objects;
import org.pkl.config.java.mapper.Named;
import org.pkl.config.java.mapper.NonNull;

public abstract class ServiceImplementationDescription {
  protected final @NonNull String name;

  protected final @NonNull ServiceImplementationType type;

  protected final double cost;

  protected final boolean local;

  protected ServiceImplementationDescription(@Named("name") @NonNull String name,
      @Named("type") @NonNull ServiceImplementationType type, @Named("cost") double cost,
      @Named("local") boolean local) {
    this.name = name;
    this.type = type;
    this.cost = cost;
    this.local = local;
  }

  public @NonNull String getName() {
    return name;
  }

  public @NonNull ServiceImplementationType getType() {
    return type;
  }

  public double getCost() {
    return cost;
  }

  public boolean isLocal() {
    return local;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    ServiceImplementationDescription other = (ServiceImplementationDescription) obj;
    if (!Objects.equals(this.name, other.name)) return false;
    if (!Objects.equals(this.type, other.type)) return false;
    if (!Objects.equals(this.cost, other.cost)) return false;
    if (!Objects.equals(this.local, other.local)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(this.name);
    result = 31 * result + Objects.hashCode(this.type);
    result = 31 * result + Objects.hashCode(this.cost);
    result = 31 * result + Objects.hashCode(this.local);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(250);
    builder.append(ServiceImplementationDescription.class.getSimpleName()).append(" {");
    appendProperty(builder, "name", this.name);
    appendProperty(builder, "type", this.type);
    appendProperty(builder, "cost", this.cost);
    appendProperty(builder, "local", this.local);
    builder.append("\n}");
    return builder.toString();
  }

  protected static void appendProperty(StringBuilder builder, String name, Object value) {
    builder.append("\n  ").append(name).append(" = ");
    String[] lines = Objects.toString(value).split("\n");
    builder.append(lines[0]);
    for (int i = 1; i < lines.length; i++) {
      builder.append("\n  ").append(lines[i]);
    }
  }

  public enum ServiceImplementationType {
    HTTP("HTTP"),

    OTHER("Other");

    private String value;

    private ServiceImplementationType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }
  }
}
