package at.ac.uibk.dps.cirrina.csml.description;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.pkl.config.java.mapper.Named;
import org.pkl.config.java.mapper.NonNull;

public final class JobDescription {
  private final @NonNull List<? extends @NonNull ServiceImplementationDescription> serviceImplementations;

  private final @NonNull CollaborativeStateMachineDescription collaborativeStateMachine;

  private final @NonNull String stateMachineName;

  private final @NonNull Map<@NonNull String, @NonNull String> localData;

  private final @NonNull List<@NonNull String> bindEventInstanceIds;

  private final @NonNull String runtimeName;

  private final double startTime;

  private final double endTime;

  public JobDescription(
      @Named("serviceImplementations") @NonNull List<? extends @NonNull ServiceImplementationDescription> serviceImplementations,
      @Named("collaborativeStateMachine") @NonNull CollaborativeStateMachineDescription collaborativeStateMachine,
      @Named("stateMachineName") @NonNull String stateMachineName,
      @Named("localData") @NonNull Map<@NonNull String, @NonNull String> localData,
      @Named("bindEventInstanceIds") @NonNull List<@NonNull String> bindEventInstanceIds,
      @Named("runtimeName") @NonNull String runtimeName, @Named("startTime") double startTime,
      @Named("endTime") double endTime) {
    this.serviceImplementations = serviceImplementations;
    this.collaborativeStateMachine = collaborativeStateMachine;
    this.stateMachineName = stateMachineName;
    this.localData = localData;
    this.bindEventInstanceIds = bindEventInstanceIds;
    this.runtimeName = runtimeName;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public @NonNull List<? extends @NonNull ServiceImplementationDescription> getServiceImplementations(
      ) {
    return serviceImplementations;
  }

  public JobDescription withServiceImplementations(
      @NonNull List<? extends @NonNull ServiceImplementationDescription> serviceImplementations) {
    return new JobDescription(serviceImplementations, collaborativeStateMachine, stateMachineName, localData, bindEventInstanceIds, runtimeName, startTime, endTime);
  }

  public @NonNull CollaborativeStateMachineDescription getCollaborativeStateMachine() {
    return collaborativeStateMachine;
  }

  public JobDescription withCollaborativeStateMachine(
      @NonNull CollaborativeStateMachineDescription collaborativeStateMachine) {
    return new JobDescription(serviceImplementations, collaborativeStateMachine, stateMachineName, localData, bindEventInstanceIds, runtimeName, startTime, endTime);
  }

  public @NonNull String getStateMachineName() {
    return stateMachineName;
  }

  public JobDescription withStateMachineName(@NonNull String stateMachineName) {
    return new JobDescription(serviceImplementations, collaborativeStateMachine, stateMachineName, localData, bindEventInstanceIds, runtimeName, startTime, endTime);
  }

  public @NonNull Map<@NonNull String, @NonNull String> getLocalData() {
    return localData;
  }

  public JobDescription withLocalData(@NonNull Map<@NonNull String, @NonNull String> localData) {
    return new JobDescription(serviceImplementations, collaborativeStateMachine, stateMachineName, localData, bindEventInstanceIds, runtimeName, startTime, endTime);
  }

  public @NonNull List<@NonNull String> getBindEventInstanceIds() {
    return bindEventInstanceIds;
  }

  public JobDescription withBindEventInstanceIds(
      @NonNull List<@NonNull String> bindEventInstanceIds) {
    return new JobDescription(serviceImplementations, collaborativeStateMachine, stateMachineName, localData, bindEventInstanceIds, runtimeName, startTime, endTime);
  }

  public @NonNull String getRuntimeName() {
    return runtimeName;
  }

  public JobDescription withRuntimeName(@NonNull String runtimeName) {
    return new JobDescription(serviceImplementations, collaborativeStateMachine, stateMachineName, localData, bindEventInstanceIds, runtimeName, startTime, endTime);
  }

  public double getStartTime() {
    return startTime;
  }

  public JobDescription withStartTime(double startTime) {
    return new JobDescription(serviceImplementations, collaborativeStateMachine, stateMachineName, localData, bindEventInstanceIds, runtimeName, startTime, endTime);
  }

  public double getEndTime() {
    return endTime;
  }

  public JobDescription withEndTime(double endTime) {
    return new JobDescription(serviceImplementations, collaborativeStateMachine, stateMachineName, localData, bindEventInstanceIds, runtimeName, startTime, endTime);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    JobDescription other = (JobDescription) obj;
    if (!Objects.equals(this.serviceImplementations, other.serviceImplementations)) return false;
    if (!Objects.equals(this.collaborativeStateMachine, other.collaborativeStateMachine)) return false;
    if (!Objects.equals(this.stateMachineName, other.stateMachineName)) return false;
    if (!Objects.equals(this.localData, other.localData)) return false;
    if (!Objects.equals(this.bindEventInstanceIds, other.bindEventInstanceIds)) return false;
    if (!Objects.equals(this.runtimeName, other.runtimeName)) return false;
    if (!Objects.equals(this.startTime, other.startTime)) return false;
    if (!Objects.equals(this.endTime, other.endTime)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(this.serviceImplementations);
    result = 31 * result + Objects.hashCode(this.collaborativeStateMachine);
    result = 31 * result + Objects.hashCode(this.stateMachineName);
    result = 31 * result + Objects.hashCode(this.localData);
    result = 31 * result + Objects.hashCode(this.bindEventInstanceIds);
    result = 31 * result + Objects.hashCode(this.runtimeName);
    result = 31 * result + Objects.hashCode(this.startTime);
    result = 31 * result + Objects.hashCode(this.endTime);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(450);
    builder.append(JobDescription.class.getSimpleName()).append(" {");
    appendProperty(builder, "serviceImplementations", this.serviceImplementations);
    appendProperty(builder, "collaborativeStateMachine", this.collaborativeStateMachine);
    appendProperty(builder, "stateMachineName", this.stateMachineName);
    appendProperty(builder, "localData", this.localData);
    appendProperty(builder, "bindEventInstanceIds", this.bindEventInstanceIds);
    appendProperty(builder, "runtimeName", this.runtimeName);
    appendProperty(builder, "startTime", this.startTime);
    appendProperty(builder, "endTime", this.endTime);
    builder.append("\n}");
    return builder.toString();
  }

  private static void appendProperty(StringBuilder builder, String name, Object value) {
    builder.append("\n  ").append(name).append(" = ");
    String[] lines = Objects.toString(value).split("\n");
    builder.append(lines[0]);
    for (int i = 1; i < lines.length; i++) {
      builder.append("\n  ").append(lines[i]);
    }
  }
}
