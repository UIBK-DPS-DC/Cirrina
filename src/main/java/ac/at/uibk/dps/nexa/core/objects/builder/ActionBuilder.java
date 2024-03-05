package ac.at.uibk.dps.nexa.core.objects.builder;

import ac.at.uibk.dps.nexa.core.Event;
import ac.at.uibk.dps.nexa.core.objects.actions.Action;
import ac.at.uibk.dps.nexa.core.objects.actions.AssignAction;
import ac.at.uibk.dps.nexa.core.objects.actions.CreateAction;
import ac.at.uibk.dps.nexa.core.objects.actions.InvokeAction;
import ac.at.uibk.dps.nexa.core.objects.actions.LockAction;
import ac.at.uibk.dps.nexa.core.objects.actions.MatchAction;
import ac.at.uibk.dps.nexa.core.objects.actions.RaiseAction;
import ac.at.uibk.dps.nexa.core.objects.actions.TimeoutAction;
import ac.at.uibk.dps.nexa.core.objects.actions.TimeoutResetAction;
import ac.at.uibk.dps.nexa.core.objects.actions.UnlockAction;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.ActionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.AssignActionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.CreateActionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.InvokeActionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.LockActionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.MatchActionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.RaiseActionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.TimeoutActionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.TimeoutResetActionClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.actions.UnlockActionClass;

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
