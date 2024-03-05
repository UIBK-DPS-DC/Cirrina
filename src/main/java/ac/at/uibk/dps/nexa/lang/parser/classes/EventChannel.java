package ac.at.uibk.dps.nexa.lang.parser.classes;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EventChannel {
  @JsonProperty("internal")
  INTERNAL,

  @JsonProperty("external")
  EXTERNAL,

  @JsonProperty("global")
  GLOBAL
}
