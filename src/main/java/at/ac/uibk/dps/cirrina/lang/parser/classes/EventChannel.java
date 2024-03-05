package at.ac.uibk.dps.cirrina.lang.parser.classes;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EventChannel {
  @JsonProperty("internal")
  INTERNAL,

  @JsonProperty("external")
  EXTERNAL,

  @JsonProperty("global")
  GLOBAL
}