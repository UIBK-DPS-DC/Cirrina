package at.ac.uibk.dps.cirrina.orchestration.orchestrators;

import at.ac.uibk.dps.cirrina.orchestration.exceptions.OrchestratorException;
import at.ac.uibk.dps.cirrina.orchestration.models.DeploymentConfig;
import at.ac.uibk.dps.cirrina.orchestration.models.RuntimeDeployment;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KubernetesOrchestrator implements Orchestrator {

  public static final String NAMESPACE = "default";
  private static final Logger logger = LogManager.getLogger(KubernetesOrchestrator.class);
  private final ApiClient apiClient;
  private final AppsV1Api appsV1Api;
  private final Set<RuntimeDeployment> activeDeployments = new HashSet<>();

  public KubernetesOrchestrator() throws OrchestratorException {
    try {
      this.apiClient = Config.defaultClient();
      Configuration.setDefaultApiClient(this.apiClient);
      this.appsV1Api = new AppsV1Api();
    } catch (IOException e) {
      throw new OrchestratorException("Failed to create Kubernetes client", e);
    }
  }

  @Override
  public RuntimeDeployment deploy(DeploymentConfig config) throws OrchestratorException {
    V1PodSpec podSpec = new V1PodSpec()
        .containers(Collections.singletonList(
            new V1Container()
                .name(config.getName())
                .image(config.getImage())
                .env(config.getEnv().entrySet().stream()
                    .map(e -> new V1EnvVar().name(e.getKey()).value(e.getValue()))
                    .toList())
                .args(config.getArgs().entrySet().stream()
                    .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                    .toList())));

    if (config.getNodes() != null && config.getNodes().length > 0) {
      podSpec.nodeSelector(Collections.singletonMap("kubernetes.io/hostname", config.getNodes()[0]));
    }

    V1Deployment deployment = new V1Deployment()
        .metadata(new V1ObjectMeta().name(config.getName()))
        .spec(new V1DeploymentSpec()
            .replicas(config.getInstances())
            .selector(new V1LabelSelector().matchLabels(Collections.singletonMap("app", config.getName())))
            .template(new V1PodTemplateSpec()
                .metadata(new V1ObjectMeta().labels(Collections.singletonMap("app", config.getName())))
                .spec(podSpec)));

    try {
      V1Deployment createdDeployment = appsV1Api.createNamespacedDeployment(NAMESPACE, deployment).execute();
      RuntimeDeployment runtimeDeployment = new RuntimeDeployment(createdDeployment.getMetadata().getUid(), config);
      activeDeployments.add(runtimeDeployment);
      return runtimeDeployment;
    } catch (ApiException e) {
      throw new OrchestratorException("Failed to create deployment", e);
    }
  }

  @Override
  public void undeploy(RuntimeDeployment deployment) throws OrchestratorException {
    try {
      appsV1Api.deleteNamespacedDeployment(deployment.getName(), NAMESPACE).execute();
      activeDeployments.remove(deployment);
    } catch (ApiException e) {
      throw new OrchestratorException("Failed to delete deployment", e);
    }
  }

  @Override
  public int getRunningInstances(RuntimeDeployment deployment) throws OrchestratorException {
    try {
      V1Deployment k8sDeployment = appsV1Api.readNamespacedDeployment(deployment.getName(), NAMESPACE).execute();
      return Objects.requireNonNull(k8sDeployment.getStatus()).getReadyReplicas() != null ?
          k8sDeployment.getStatus().getReadyReplicas() :
          0;
    } catch (ApiException e) {
      throw new OrchestratorException("Failed to get deployment status", e);
    }
  }

  @Override
  public void scale(RuntimeDeployment deployment, int instances) throws OrchestratorException {
    try {
      V1Deployment k8sDeployment = appsV1Api.readNamespacedDeployment(deployment.getName(), NAMESPACE).execute();

      V1DeploymentSpec spec = k8sDeployment.getSpec();
      if (spec == null) {
        spec = new V1DeploymentSpec();
        k8sDeployment.setSpec(spec);
      }
      spec.setReplicas(instances);

      appsV1Api.replaceNamespacedDeployment(deployment.getName(), NAMESPACE, k8sDeployment).execute();
      deployment.setInstances(instances);
    } catch (ApiException e) {
      throw new OrchestratorException("Failed to scale deployment", e);
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
        logger.error("Failed to undeploy deployment: {}", deployment.getName(), e);
      }
    }

    try {
      this.apiClient.getHttpClient().connectionPool().evictAll();
      this.apiClient.getHttpClient().dispatcher().executorService().shutdown();
    } catch (Exception e) {
      throw new OrchestratorException("Failed to close Kubernetes client", e);
    }
  }
}
