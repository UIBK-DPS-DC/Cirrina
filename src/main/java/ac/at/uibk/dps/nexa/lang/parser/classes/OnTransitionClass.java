package ac.at.uibk.dps.nexa.lang.parser.classes;

import javax.validation.constraints.NotNull;

/**
 * On transition construct. Represents a transition that is to be taken based on a received event.
 * <p>
 * Keywords:
 * <table border="1">
 *  <tr><th>Keyword</th><th>Description</th><th>Required</th></tr>
 *  <tr><td>event</td><td>Event</td><td>Yes</td></tr>
 * </table>
 * </p>
 *  Example:
 *  <pre>
 *    {
 *      target: 'State Name',
 *      guards: [...],
 *      actions: [...],
 *      event: 'Event Name'
 *    }
 *  </pre>
 * </p>
 *
 * @since CSML 0.1.
 */
public class OnTransitionClass extends TransitionClass {

  /**
   * The event that triggers this on transition.
   */
  @NotNull
  public String event;
}
