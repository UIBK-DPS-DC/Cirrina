package at.ac.uibk.dps.cirrina.orchestration.orchestrators;

import at.ac.uibk.dps.cirrina.orchestration.exceptions.OrchestratorException;
import at.ac.uibk.dps.cirrina.orchestration.models.DeploymentConfig;
import at.ac.uibk.dps.cirrina.orchestration.models.RuntimeDeployment;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateServiceResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.ContainerSpec;
import com.github.dockerjava.api.model.Service;
import com.github.dockerjava.api.model.ServiceModeConfig;
import com.github.dockerjava.api.model.ServicePlacement;
import com.github.dockerjava.api.model.ServiceReplicatedModeOptions;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.api.model.TaskSpec;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DockerSwarmOrchestrator implements Orchestrator {

  private static final Logger logger = LogManager.getLogger(DockerSwarmOrchestrator.class);
  private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(30);
  private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(45);

  private final DockerClient dockerClient;
  private final Set<RuntimeDeployment> activeDeployments = new HashSet<>();

  public DockerSwarmOrchestrator() {
    DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

    DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig())
        .connectionTimeout(CONNECTION_TIMEOUT)
        .responseTimeout(RESPONSE_TIMEOUT)
        .build();
    this.dockerClient = DockerClientBuilder.getInstance(config)
        .withDockerHttpClient(httpClient)
        .build();
  }

  @Override
  public RuntimeDeployment deploy(DeploymentConfig config) throws OrchestratorException {
    List<String> commandArgs = new ArrayList<>();
    config.getArgs().forEach((key, value) -> {
      commandArgs.add(key);
      commandArgs.add(value);
    });

    TaskSpec taskSpec = new TaskSpec()
        .withContainerSpec(new ContainerSpec()
            .withImage(config.getImage())
            .withEnv(config.getEnv().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .toList())
            .withArgs(commandArgs));

    if (config.getNodes() != null && config.getNodes().length > 0) {
      taskSpec.withPlacement(new ServicePlacement()
          .withConstraints(List.of("node.hostname==" + String.join("|", config.getNodes()))));
    }

    ServiceSpec serviceSpec = new ServiceSpec()
        .withName(config.getName())
        .withTaskTemplate(taskSpec)
        .withMode(new ServiceModeConfig()
            .withReplicated(new ServiceReplicatedModeOptions()
                .withReplicas(config.getInstances())));

    try {
      CreateServiceResponse response = dockerClient.createServiceCmd(serviceSpec).exec();
      if (response.getId() == null) {
        throw new OrchestratorException("Failed to create service: No service ID returned");
      }
      RuntimeDeployment deployment = new RuntimeDeployment(response.getId(), config);
      activeDeployments.add(deployment);
      return deployment;
    } catch (DockerException e) {
      throw new OrchestratorException("Failed to create service", e);
    }
  }

  @Override
  public void undeploy(RuntimeDeployment deployment) throws OrchestratorException {
    try {
      dockerClient.removeServiceCmd(deployment.getName()).exec();
      activeDeployments.remove(deployment);
    } catch (DockerException e) {
      throw new OrchestratorException("Failed to remove service", e);
    }
  }

  @Override
  public int getRunningInstances(RuntimeDeployment deployment) throws OrchestratorException {
    // Note: This is not the live number of instances, but the number of replicas specified in the service spec.
    try {
      Service service = dockerClient.inspectServiceCmd(deployment.getName()).exec();
      return (int) service.getSpec().getMode().getReplicated().getReplicas();
    } catch (DockerException e) {
      throw new OrchestratorException("Failed to inspect service", e);
    }
  }

  @Override
  public void scale(RuntimeDeployment deployment, int instances) throws OrchestratorException {
    try {
      Service service = dockerClient.inspectServiceCmd(deployment.getName()).exec();
      ServiceSpec serviceSpec = service.getSpec();

      serviceSpec.getMode().getReplicated().withReplicas(instances);

      dockerClient.updateServiceCmd(deployment.getName(), serviceSpec)
          .withVersion(service.getVersion().getIndex())
          .exec();

      deployment.setInstances(instances);
    } catch (DockerException e) {
      throw new OrchestratorException("Failed to scale service", e);
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
        logger.error("Failed to undeploy service: " + deployment.getName(), e);
      }
    }

    try {
      this.dockerClient.close();
    } catch (IOException e) {
      throw new OrchestratorException("Failed to close Docker client", e);
    }
  }
}
