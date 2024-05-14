package at.ac.uibk.dps.cirrina.runtime.job;

import at.ac.uibk.dps.cirrina.io.description.DescriptionParser;

/**
 * Job description parser. Parses job description JSON data files.
 */
public class JobDescriptionParser extends DescriptionParser<JobDescription> {

  /**
   * Initializes the parser.
   */
  public JobDescriptionParser() {
    super(JobDescription.class);
  }
}
