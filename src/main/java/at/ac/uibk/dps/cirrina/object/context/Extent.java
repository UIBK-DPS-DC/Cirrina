package at.ac.uibk.dps.cirrina.object.context;

import java.util.List;
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
}
