package at.ac.uibk.dps.cirrina.csml.description.action;

import at.ac.uibk.dps.cirrina.csml.description.helper.ActionOrActionReferenceDescription;
import jakarta.validation.constraints.NotNull;

public final class ActionReferenceDescription implements ActionOrActionReferenceDescription {

  @NotNull
  public String reference;

  @Override
  public String toString() {
    return reference;
  }
}
