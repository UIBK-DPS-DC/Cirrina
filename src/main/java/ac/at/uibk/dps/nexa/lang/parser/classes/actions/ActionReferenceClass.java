package ac.at.uibk.dps.nexa.lang.parser.classes.actions;

import jakarta.validation.constraints.NotNull;

public class ActionReferenceClass implements ActionOrActionReferenceClass {

  @NotNull
  public String reference;
}
