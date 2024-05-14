package at.ac.uibk.dps.cirrina.runtime.job;

import at.ac.uibk.dps.cirrina.core.exception.CirrinaException;

/**
 * Job listener, informed when a new job is submitted.
 */
public interface JobListener {

  /**
   * Called upon the arrival of a new job, will instantiate a new state machine if capable and appropriate.
   *
   * @param job New job.
   * @throws CirrinaException In case of error.
   */
  void newJob(Job job) throws CirrinaException;
}
