package at.ac.uibk.dps.cirrina.lang.parser.classes.helper;

import at.ac.uibk.dps.cirrina.lang.parser.classes.StateClass;
import at.ac.uibk.dps.cirrina.lang.parser.classes.StateMachineClass;
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
