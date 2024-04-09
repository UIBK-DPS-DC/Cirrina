package at.ac.uibk.dps.cirrina.core.lang.classes.action;

import at.ac.uibk.dps.cirrina.core.lang.classes.helper.ActionOrActionReferenceClass;
import jakarta.validation.constraints.NotNull;

public final class ActionReferenceClass implements ActionOrActionReferenceClass {

  @NotNull
  public String reference;
}