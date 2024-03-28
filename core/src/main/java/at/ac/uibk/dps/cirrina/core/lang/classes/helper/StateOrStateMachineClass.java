package at.ac.uibk.dps.cirrina.core.lang.classes.helper;

import at.ac.uibk.dps.cirrina.core.lang.classes.StateClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.StateMachineClass;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A helper interface describing a type that can be a state or a state machine.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = StateClass.class)
@JsonSubTypes({
    @JsonSubTypes.Type(StateClass.class),
    @JsonSubTypes.Type(StateMachineClass.class)
})
public interface StateOrStateMachineClass {

}
