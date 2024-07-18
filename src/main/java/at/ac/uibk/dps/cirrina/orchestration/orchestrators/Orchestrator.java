package at.ac.uibk.dps.cirrina.orchestration.orchestrators;

import at.ac.uibk.dps.cirrina.orchestration.exceptions.OrchestratorException;
import at.ac.uibk.dps.cirrina.orchestration.models.DeploymentConfig;
import at.ac.uibk.dps.cirrina.orchestration.models.RuntimeDeployment;
import java.util.Set;

public interface Orchestrator extends AutoCloseable {

  RuntimeDeployment deploy(DeploymentConfig config) throws OrchestratorException;

  void undeploy(RuntimeDeployment deployment) throws OrchestratorException;

  int getRunningInstances(RuntimeDeployment deployment) throws OrchestratorException;

  void scale(RuntimeDeployment deployment, int instances) throws OrchestratorException;

  Set<RuntimeDeployment> getActiveDeployments();

  @Override
  void close() throws OrchestratorException;
}
