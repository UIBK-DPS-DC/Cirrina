package at.ac.uibk.dps.cirrina.csml.keyword;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EventChannel {
  @JsonProperty("internal")
  INTERNAL,

  @JsonProperty("external")
  EXTERNAL,

  @JsonProperty("global")
  GLOBAL,

  @JsonProperty("peripheral")
  PERIPHERAL;

  @Override
  public String toString() throws IllegalStateException {
    switch (this) {
      case INTERNAL -> {
        return "internal";
      }
      case EXTERNAL -> {
        return "external";
      }
      case GLOBAL -> {
        return "global";
      }
      case PERIPHERAL -> {
        return "peripheral";
      }
    }

    throw new IllegalStateException();
  }
}
