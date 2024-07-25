package at.ac.uibk.dps.cirrina.csml.description;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.Objects;
import org.pkl.config.java.mapper.Named;
import org.pkl.config.java.mapper.NonNull;

public final class HttpServiceImplementationDescription extends ServiceImplementationDescription {
  private final @NonNull String scheme;

  private final @NonNull String host;

  private final long port;

  private final @NonNull String endPoint;

  private final @NonNull Method method;

  public HttpServiceImplementationDescription(@Named("name") @NonNull String name,
      @Named("type") ServiceImplementationDescription. @NonNull ServiceImplementationType type,
      @Named("cost") double cost, @Named("local") boolean local,
      @Named("scheme") @NonNull String scheme, @Named("host") @NonNull String host,
      @Named("port") long port, @Named("endPoint") @NonNull String endPoint,
      @Named("method") @NonNull Method method) {
    super(name, type, cost, local);
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.endPoint = endPoint;
    this.method = method;
  }

  public HttpServiceImplementationDescription withName(@NonNull String name) {
    return new HttpServiceImplementationDescription(name, type, cost, local, scheme, host, port, endPoint, method);
  }

  public HttpServiceImplementationDescription withType(
      ServiceImplementationDescription. @NonNull ServiceImplementationType type) {
    return new HttpServiceImplementationDescription(name, type, cost, local, scheme, host, port, endPoint, method);
  }

  public HttpServiceImplementationDescription withCost(double cost) {
    return new HttpServiceImplementationDescription(name, type, cost, local, scheme, host, port, endPoint, method);
  }

  public HttpServiceImplementationDescription withLocal(boolean local) {
    return new HttpServiceImplementationDescription(name, type, cost, local, scheme, host, port, endPoint, method);
  }

  public @NonNull String getScheme() {
    return scheme;
  }

  public HttpServiceImplementationDescription withScheme(@NonNull String scheme) {
    return new HttpServiceImplementationDescription(name, type, cost, local, scheme, host, port, endPoint, method);
  }

  public @NonNull String getHost() {
    return host;
  }

  public HttpServiceImplementationDescription withHost(@NonNull String host) {
    return new HttpServiceImplementationDescription(name, type, cost, local, scheme, host, port, endPoint, method);
  }

  public long getPort() {
    return port;
  }

  public HttpServiceImplementationDescription withPort(long port) {
    return new HttpServiceImplementationDescription(name, type, cost, local, scheme, host, port, endPoint, method);
  }

  public @NonNull String getEndPoint() {
    return endPoint;
  }

  public HttpServiceImplementationDescription withEndPoint(@NonNull String endPoint) {
    return new HttpServiceImplementationDescription(name, type, cost, local, scheme, host, port, endPoint, method);
  }

  public @NonNull Method getMethod() {
    return method;
  }

  public HttpServiceImplementationDescription withMethod(@NonNull Method method) {
    return new HttpServiceImplementationDescription(name, type, cost, local, scheme, host, port, endPoint, method);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    HttpServiceImplementationDescription other = (HttpServiceImplementationDescription) obj;
    if (!Objects.equals(this.name, other.name)) return false;
    if (!Objects.equals(this.type, other.type)) return false;
    if (!Objects.equals(this.cost, other.cost)) return false;
    if (!Objects.equals(this.local, other.local)) return false;
    if (!Objects.equals(this.scheme, other.scheme)) return false;
    if (!Objects.equals(this.host, other.host)) return false;
    if (!Objects.equals(this.port, other.port)) return false;
    if (!Objects.equals(this.endPoint, other.endPoint)) return false;
    if (!Objects.equals(this.method, other.method)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(this.name);
    result = 31 * result + Objects.hashCode(this.type);
    result = 31 * result + Objects.hashCode(this.cost);
    result = 31 * result + Objects.hashCode(this.local);
    result = 31 * result + Objects.hashCode(this.scheme);
    result = 31 * result + Objects.hashCode(this.host);
    result = 31 * result + Objects.hashCode(this.port);
    result = 31 * result + Objects.hashCode(this.endPoint);
    result = 31 * result + Objects.hashCode(this.method);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(500);
    builder.append(HttpServiceImplementationDescription.class.getSimpleName()).append(" {");
    appendProperty(builder, "name", this.name);
    appendProperty(builder, "type", this.type);
    appendProperty(builder, "cost", this.cost);
    appendProperty(builder, "local", this.local);
    appendProperty(builder, "scheme", this.scheme);
    appendProperty(builder, "host", this.host);
    appendProperty(builder, "port", this.port);
    appendProperty(builder, "endPoint", this.endPoint);
    appendProperty(builder, "method", this.method);
    builder.append("\n}");
    return builder.toString();
  }

  public enum Method {
    GET("GET"),

    POST("POST");

    private String value;

    private Method(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }
  }
}
