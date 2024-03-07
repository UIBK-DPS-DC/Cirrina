package at.ac.uibk.dps.cirrina.lang.parser.classes.actions;

import at.ac.uibk.dps.cirrina.lang.parser.classes.EventClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.context.ContextVariableClass;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class InvokeActionClass extends ActionClass {

  @NotNull
  public String serviceType;

  @JsonSetter(nulls = Nulls.SKIP)
  public boolean isLocal = false;

  public Optional<ContextVariableClass> input = Optional.empty();

  public Optional<List<EventClass>> done = Optional.empty();

  /**
   * The optional output variable.
   * <p>
   * Used to automatically store service output to a local context variable. The variable must exist
   * at runtime.
   * </p>
   */
  public Optional<String> output = Optional.empty(); // TODO: Felix this should be a context variable, I just introduced ContextVariable
}
