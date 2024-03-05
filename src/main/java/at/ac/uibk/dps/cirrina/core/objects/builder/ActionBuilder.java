package at.ac.uibk.dps.cirrina.core.objects.builder;

import at.ac.uibk.dps.cirrina.core.Event;
import at.ac.uibk.dps.cirrina.core.objects.actions.Action;
import at.ac.uibk.dps.cirrina.core.objects.actions.AssignAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.CreateAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.InvokeAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.LockAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.MatchAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.RaiseAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.TimeoutAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.TimeoutResetAction;
import at.ac.uibk.dps.cirrina.core.objects.actions.UnlockAction;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.ActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.AssignActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.CreateActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.InvokeActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.LockActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.MatchActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.RaiseActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.TimeoutActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.TimeoutResetActionClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.actions.UnlockActionClass;

public class ActionBuilder {

  private final ActionClass actionClass;

  public ActionBuilder(ActionClass actionClass) {
    this.actionClass = actionClass;
  }

  public Action build() throws IllegalArgumentException {
    switch (actionClass) {
      case AssignActionClass assign -> {
        return new AssignAction(assign.name);
      }
      case CreateActionClass create -> {
        return new CreateAction(create.name);
      }
      case InvokeActionClass invoke -> {
        return new InvokeAction(invoke.name);
      }
      case LockActionClass lock -> {
        return new LockAction(lock.name);
      }
      case MatchActionClass match -> {
        return new MatchAction(match.name);
      }
      case RaiseActionClass raise -> {
        return new RaiseAction(raise.name, new Event(raise.event.name, raise.event.channel, raise.event.data));
      }
      case TimeoutActionClass timeout -> {
        return new TimeoutAction(timeout.name);
      }
      case TimeoutResetActionClass timeoutReset -> {
        return new TimeoutResetAction(timeoutReset.name);
      }
      case UnlockActionClass unlock -> {
        return new UnlockAction(unlock.name);
      }
      default -> throw new IllegalStateException("Unexpected value: " + actionClass.type);
    }
  }
}
