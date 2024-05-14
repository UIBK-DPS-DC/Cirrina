package at.ac.uibk.dps.cirrina.csml.description.helper;

import at.ac.uibk.dps.cirrina.csml.description.StateDescription;
import at.ac.uibk.dps.cirrina.csml.description.StateMachineDescription;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A helper interface describing a type that can be a state or a state machine.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = StateDescription.class)
@JsonSubTypes({
    @JsonSubTypes.Type(StateDescription.class),
    @JsonSubTypes.Type(StateMachineDescription.class)
})
public interface StateOrStateMachineDescription {

}
