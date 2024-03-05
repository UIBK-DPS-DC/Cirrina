package at.ac.uibk.dps.cirrina.lang.parser.classes.actions;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class InvokeActionClass extends ActionClass {

  @NotNull
  public String serviceType;

  @JsonSetter(nulls = Nulls.SKIP)
  public boolean isLocal = false;

  public Optional<Map<String, String>> input = Optional.empty();

  public Optional<List<String>> done = Optional.empty();

  /**
   * The optional output variable.
   * <p>
   * Used to automatically store service output to a local context variable. The variable must exist at runtime.
   * </p>
   */
  public Optional<String> output = Optional.empty();
}
