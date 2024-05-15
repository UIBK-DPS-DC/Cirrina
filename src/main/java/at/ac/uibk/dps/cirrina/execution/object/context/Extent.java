package at.ac.uibk.dps.cirrina.execution.object.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Extent {

  private List<Context> extent;

  public Extent() {
    extent = new ArrayList<>();
  }

  public Extent(Context low) {
    extent = List.of(low);
  }

  public Extent(Context low, Context high) {
    extent = List.of(low, high);
  }

  public Extent(List<Context> low, Context high) {
    extent = Stream.concat(
            low.stream(),
            Stream.of(high))
        .toList();
  }

  public void setOrCreate(String name, Object value) throws IOException {
    final var last = extent.getLast();

    try {
      last.assign(name, value);
    } catch (IOException e) {
      last.create(name, value);
    }
  }

  public void trySet(String name, Object value) throws IOException {
    var exceptions = extent.reversed().stream()
        .map(context -> {
          try {
            context.assign(name, value);
            return Optional.<IOException>empty();
          } catch (IOException e) {
            return Optional.of(e);
          }
        })
        .flatMap(Optional::stream)
        .toList();

    if (exceptions.size() == extent.size()) {
      throw exceptions.getLast();
    }
  }

  public Extent extend(Context high) {
    return new Extent(extent, high);
  }

  public Context getLow() {
    return extent.getFirst();
  }

  public Context getHigh() {
    return extent.getLast();
  }

  public Optional<Object> resolve(String name) {
    return extent.reversed().stream()
        .map(context -> {
          try {
            return Optional.ofNullable(context.get(name));
          } catch (IOException e) {
            return Optional.empty();
          }
        })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }
}
