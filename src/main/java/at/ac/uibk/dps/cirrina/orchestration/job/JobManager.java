package at.ac.uibk.dps.cirrina.orchestration.job;

import at.ac.uibk.dps.cirrina.orchestration.exceptions.OrchestratorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JobManager implements AutoCloseable {

  private static final Logger logger = LogManager.getLogger(JobManager.class);
  private static final String JOBS_PATH = "/jobs";

  private final CuratorFramework client;
  private final ObjectMapper objectMapper;

  public JobManager(String zookeeperConnectionString) {
    this.client = CuratorFrameworkFactory.newClient(
        zookeeperConnectionString,
        new ExponentialBackoffRetry(1000, 3)
    );
    this.client.start();
    this.objectMapper = new ObjectMapper();
  }

  public void submitJob(String jobId, JsonNode jobDescription) throws OrchestratorException {
    try {
      String jobPath = "%s/%s".formatted(JOBS_PATH, jobId);
      byte[] jobData = objectMapper.writeValueAsString(jobDescription).getBytes(StandardCharsets.UTF_8);
      client.create().creatingParentsIfNeeded().forPath(jobPath, jobData);
      logger.info("Job submitted: {} for runtime: {}", jobPath, jobDescription.get("runtimeName"));
    } catch (Exception e) {
      throw new OrchestratorException("Failed to submit job", e);
    }
  }

  public void deleteJob(String jobId) throws OrchestratorException {
    try {
      String jobPath = "%s/%s".formatted(JOBS_PATH, jobId);
      client.delete().forPath(jobPath);
      logger.info("Job deleted: {}", jobPath);
    } catch (Exception e) {
      throw new OrchestratorException("Failed to delete job", e);
    }
  }

  @Override
  public void close() {
    if (client != null) {
      client.close();
    }
  }
}
