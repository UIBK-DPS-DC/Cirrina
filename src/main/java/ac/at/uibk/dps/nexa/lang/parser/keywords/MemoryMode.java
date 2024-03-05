package ac.at.uibk.dps.nexa.lang.parser.keywords;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Memory mode keyword.
 *
 * @since CSML 0.1.
 */
public enum MemoryMode {
  /**
   * Distributed memory mode.
   */
  @JsonProperty("distributed")
  DISTRIBUTED,

  /**
   * Shared memory mode.
   */
  @JsonProperty("shared")
  SHARED
}
