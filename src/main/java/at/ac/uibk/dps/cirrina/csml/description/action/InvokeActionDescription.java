package at.ac.uibk.dps.cirrina.csml.description.action;

import at.ac.uibk.dps.cirrina.csml.description.context.ContextVariableDescription;
import at.ac.uibk.dps.cirrina.csml.description.context.ContextVariableReferenceDescription;
import at.ac.uibk.dps.cirrina.csml.description.event.EventDescription;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(using = JsonDeserializer.None.class)
public final class InvokeActionDescription extends ActionDescription {

  @NotNull
  public String serviceType;

  @JsonSetter(nulls = Nulls.SKIP)
  public boolean isLocal = false;

  public List<ContextVariableDescription> input = List.of();

  public List<EventDescription> done = List.of();

  /**
   * The optional output variable.
   * <p>
   * Used to automatically store service output to a local context variable. The variable must exist at runtime.
   */
  public Optional<ContextVariableReferenceDescription> output = Optional.empty();
}
