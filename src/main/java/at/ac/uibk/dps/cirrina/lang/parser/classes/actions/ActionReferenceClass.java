package at.ac.uibk.dps.cirrina.lang.parser.classes.actions;

import jakarta.validation.constraints.NotNull;

public class ActionReferenceClass implements ActionOrActionReferenceClass {

  @NotNull
  public String reference;
}