package at.ac.uibk.dps.cirrina.object.context;

import at.ac.uibk.dps.cirrina.exception.RuntimeException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Extent {

  private List<Context> extent;

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

  public Extent extend(Context high) {
    return new Extent(extent, high);
  }

  public Context getLow() {
    return extent.getFirst();
  }

  public Context getHigh() {
    return extent.getLast();
  }

  public Optional<Object> resolve(String key) {

    return extent.reversed().stream()
        .map(ctx -> {
          try {
            return Optional.of(ctx.get(key));
          } catch (RuntimeException e) {
            return Optional.empty();
          }
        })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }
}
