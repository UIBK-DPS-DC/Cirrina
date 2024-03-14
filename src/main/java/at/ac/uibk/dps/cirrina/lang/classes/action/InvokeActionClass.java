package at.ac.uibk.dps.cirrina.lang.classes.action;

import at.ac.uibk.dps.cirrina.lang.classes.context.ContextVariableClass;
import at.ac.uibk.dps.cirrina.lang.classes.context.ContextVariableReference;
import at.ac.uibk.dps.cirrina.lang.classes.event.EventClass;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class InvokeActionClass extends ActionClass {

  @NotNull
  public String serviceType;

  @JsonSetter(nulls = Nulls.SKIP)
  public boolean isLocal = false;

  public List<ContextVariableClass> input = List.of();

  public List<EventClass> done = List.of();

  /**
   * The optional output variable.
   * <p>
   * Used to automatically store service output to a local context variable. The variable must exist at runtime.
   */
  public Optional<ContextVariableReference> output = Optional.empty();
}
