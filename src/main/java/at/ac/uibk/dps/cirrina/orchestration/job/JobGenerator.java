package at.ac.uibk.dps.cirrina.orchestration.job;

import at.ac.uibk.dps.cirrina.orchestration.exceptions.OrchestratorException;
import com.fasterxml.jackson.databind.JsonNode;

public interface JobGenerator {

  /**
   * Generates a job description.
   *
   * @param runtime The runtime name for this job.
   * @return A JsonNode object representing the job description.
   * @throws OrchestratorException if there's an error generating the job description.
   */
  JsonNode generateJob(String runtime) throws OrchestratorException;
}
