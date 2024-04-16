package at.ac.uibk.dps.cirrina.core.io.plantuml;

import at.ac.uibk.dps.cirrina.core.object.action.Action;
import at.ac.uibk.dps.cirrina.core.object.action.AssignAction;
import at.ac.uibk.dps.cirrina.core.object.action.CreateAction;
import at.ac.uibk.dps.cirrina.core.object.action.InvokeAction;
import at.ac.uibk.dps.cirrina.core.object.action.MatchAction;
import at.ac.uibk.dps.cirrina.core.object.action.RaiseAction;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutAction;
import at.ac.uibk.dps.cirrina.core.object.action.TimeoutResetAction;

public class ActionColors {

  private ActionColors() {
  }

  public static String getActionColor(Action action) {
    return switch (action) {
      case AssignAction assignAction -> "#27AE60"; // Green
      case CreateAction createAction -> "#F39C12"; // Orange
      case InvokeAction invokeAction -> "#1F618D"; // Blue
      case MatchAction matchAction -> "#1ABC9C"; // Turquoise
      case RaiseAction raiseAction -> "#9B59B6"; // Purple
      case TimeoutAction timeoutAction -> "#34495E";// Dark Gray
      case TimeoutResetAction timeoutResetAction -> "#34495E"; // Dark Gray
      default -> "#000000"; // Black
    };
  }

}
