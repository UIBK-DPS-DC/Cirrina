package at.ac.uibk.dps.cirrina.core.object.guard;

import at.ac.uibk.dps.cirrina.core.lang.classes.guard.GuardClass;
import at.ac.uibk.dps.cirrina.core.object.expression.ExpressionBuilder;

/**
 * Action builder, used to build action objects.
 */
public final class GuardBuilder {

  /**
   * The action class to build from.
   */
  private final GuardClass guardClass;

  /**
   * Initializes an action builder.
   *
   * @param guardClass Guard class.
   */
  private GuardBuilder(GuardClass guardClass) {
    this.guardClass = guardClass;
  }

  /**
   * Creates an guard builder.
   *
   * @param guardClass Guard class.
   * @return Guard builder.
   */
  public static GuardBuilder from(GuardClass guardClass) {
    return new GuardBuilder(guardClass);
  }

  /**
   * Builds the guard.
   *
   * @return The built guard.
   * @throws IllegalArgumentException In case the action could not be built.
   * @throws IllegalStateException    In case of an unexpected state.
   */
  public Guard build() throws IllegalArgumentException, IllegalStateException {
    var guard = new Guard(guardClass.name, ExpressionBuilder.from(guardClass.expression).build());

    return guard;
  }
}
