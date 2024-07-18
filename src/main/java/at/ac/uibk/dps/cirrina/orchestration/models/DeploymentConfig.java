package at.ac.uibk.dps.cirrina.orchestration.models;

import java.util.Map;

public class DeploymentConfig {

  private final String name;
  private final int instances;
  private final String image;
  private final Map<String, String> env;
  private final Map<String, String> args;
  // If `nodes` is null, the deployment will be scheduled on any available node.
  // If `nodes` is a non-empty array, the deployment will be scheduled on the specified nodes.
  private final String[] nodes;

  public DeploymentConfig(String name, int instances, String image, Map<String, String> env, Map<String, String> args, String[] nodes) {
    this.name = name;
    this.instances = instances;
    this.image = image;
    this.env = env;
    this.args = args;
    this.nodes = nodes;
  }

  public String getName() {
    return name;
  }

  public int getInstances() {
    return instances;
  }

  public String getImage() {
    return image;
  }

  public Map<String, String> getEnv() {
    return env;
  }

  public Map<String, String> getArgs() {
    return args;
  }

  public String[] getNodes() {
    return nodes;
  }
}
