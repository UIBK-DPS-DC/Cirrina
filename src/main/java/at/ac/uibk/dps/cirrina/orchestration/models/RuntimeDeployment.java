package at.ac.uibk.dps.cirrina.orchestration.models;

import java.util.Objects;

public class RuntimeDeployment {

  private final String id;
  private final String name;
  private final String image;
  private final String[] nodes;
  private int instances;

  public RuntimeDeployment(String id, DeploymentConfig config) {
    this.id = id;
    this.name = config.getName();
    this.instances = config.getInstances();
    this.image = config.getImage();
    this.nodes = config.getNodes();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getInstances() {
    return instances;
  }

  public void setInstances(int instances) {
    this.instances = instances;
  }

  public String getImage() {
    return image;
  }

  public String[] getNodes() {
    return nodes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RuntimeDeployment that = (RuntimeDeployment) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
