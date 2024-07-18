package at.ac.uibk.dps.cirrina.orchestration.orchestrators;

import at.ac.uibk.dps.cirrina.orchestration.exceptions.OrchestratorException;
import at.ac.uibk.dps.cirrina.orchestration.models.DeploymentConfig;
import at.ac.uibk.dps.cirrina.orchestration.models.RuntimeDeployment;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.NodeListStub;
import com.hashicorp.nomad.apimodel.Task;
import com.hashicorp.nomad.apimodel.TaskGroup;
import com.hashicorp.nomad.javasdk.EvaluationResponse;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadException;
import com.hashicorp.nomad.javasdk.ServerQueryResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NomadOrchestrator implements Orchestrator {

  private static final Logger logger = LogManager.getLogger(NomadOrchestrator.class);
  private final NomadApiClient nomadClient;
  private final Set<RuntimeDeployment> activeDeployments = new HashSet<>();

  public NomadOrchestrator(String nomadAddress) {
    this.nomadClient = new NomadApiClient(nomadAddress);
    logger.info("NomadOrchestrator initialized with address: {}", nomadAddress);
  }

  private List<NodeListStub> getAvailableDatacenters() throws OrchestratorException {
    try {
      ServerQueryResponse<List<NodeListStub>> nodesResponse = nomadClient.getNodesApi().list();
      return nodesResponse.getValue();
    } catch (IOException | NomadException e) {
      throw new OrchestratorException("Failed to fetch available datacenters", e);
    }
  }

  private List<String> selectDatacenters(DeploymentConfig config) throws OrchestratorException {
    List<NodeListStub> availableNodesList = getAvailableDatacenters();
    List<String> availableDatacenters = availableNodesList.stream().map(NodeListStub::getDatacenter).collect(Collectors.toList());

    if (config.getNodes() != null && config.getNodes().length > 0) {
      return Arrays.stream(config.getNodes())
          .filter(availableDatacenters::contains)
          .collect(Collectors.toList());
    } else {
      // If no nodes are specified, use all available datacenters
      return availableDatacenters;
    }
  }

  @Override
  public RuntimeDeployment deploy(DeploymentConfig config) throws OrchestratorException {
    List<String> args = new ArrayList<>();
    config.getArgs().forEach((key, value) -> {
      args.add(key);
      args.add(value);
    });

    List<String> datacenters = selectDatacenters(config);
    if (datacenters.isEmpty()) {
      throw new OrchestratorException("No valid datacenters found for deployment");
    }

    Map<String, Object> taskConfig = new HashMap<>();
    taskConfig.put("image", config.getImage());
    taskConfig.put("args", args);

    Job job = new Job()
        .setId(config.getName())
        .setName(config.getName())
        .setType("service")
        .setDatacenters(datacenters)
        .addTaskGroups(new TaskGroup()
            .setName(config.getName())
            .setCount(config.getInstances())
            .addTasks(new Task()
                .setName(config.getName())
                .setDriver("docker")
                .setConfig(taskConfig)
                .setEnv(config.getEnv())));

    try {
      logger.info("Deploying job: {}", config.getName());
      EvaluationResponse response = nomadClient.getJobsApi().register(job);
      String evaluationId = response.getValue();
      if (evaluationId != null && !evaluationId.isEmpty()) {
        RuntimeDeployment deployment = new RuntimeDeployment(evaluationId, config);
        activeDeployments.add(deployment);
        logger.info("Job deployed successfully: {}", deployment.getName());
        return deployment;
      } else {
        throw new OrchestratorException("Failed to deploy job: No evaluation ID returned");
      }
    } catch (IOException | NomadException e) {
      logger.error("Error deploying job: {}", config.getName(), e);
      throw new OrchestratorException("Failed to deploy job", e);
    }
  }

  @Override
  public void undeploy(RuntimeDeployment deployment) throws OrchestratorException {
    try {
      nomadClient.getJobsApi().deregister(deployment.getName());
      activeDeployments.remove(deployment);
    } catch (IOException | NomadException e) {
      throw new OrchestratorException("Failed to undeploy job", e);
    }
  }

  @Override
  public int getRunningInstances(RuntimeDeployment deployment) throws OrchestratorException {
    try {
      return nomadClient.getJobsApi()
          .summary(deployment.getName()).getValue().getSummary().get(deployment.getName()).getRunning();
    } catch (IOException | NomadException e) {
      throw new OrchestratorException("Failed to get running instances", e);
    }
  }

  @Override
  public void scale(RuntimeDeployment deployment, int instances) throws OrchestratorException {
    try {
      ServerQueryResponse<Job> response = nomadClient.getJobsApi().info(deployment.getName());
      Job job = response.getValue();
      if (job != null && job.getTaskGroups() != null && !job.getTaskGroups().isEmpty()) {
        job.getTaskGroups().getFirst().setCount(instances);
        nomadClient.getJobsApi().register(job);
        deployment.setInstances(instances);
      } else {
        throw new OrchestratorException("Failed to scale job: Job or task group not found");
      }
    } catch (IOException | NomadException e) {
      throw new OrchestratorException("Failed to scale job", e);
    }
  }

  @Override
  public Set<RuntimeDeployment> getActiveDeployments() {
    return new HashSet<>(activeDeployments);
  }

  @Override
  public void close() throws OrchestratorException {
    for (RuntimeDeployment deployment : getActiveDeployments()) {
      try {
        undeploy(deployment);
      } catch (OrchestratorException e) {
        logger.error("Failed to undeploy job during shutdown: {}", deployment.getName(), e);
      }
    }
    try {
      nomadClient.close();
      logger.info("NomadOrchestrator closed successfully");
    } catch (IOException e) {
      logger.error("Error closing Nomad client", e);
      throw new OrchestratorException("Failed to close Nomad client", e);
    }
  }
}
