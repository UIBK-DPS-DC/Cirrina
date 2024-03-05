package at.ac.uibk.dps.cirrina.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Common {

  public static <T> Set<T> getListDuplicates(List<T> list) {
    var elements = new HashSet<>();
    return list.stream()
        .filter(n -> !elements.add(n))
        .collect(Collectors.toSet());
  }
}
