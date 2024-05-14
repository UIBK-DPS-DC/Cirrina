package at.ac.uibk.dps.cirrina.csml.description.helper;

import at.ac.uibk.dps.cirrina.csml.description.guard.GuardDescription;
import at.ac.uibk.dps.cirrina.csml.description.guard.GuardReferenceDescription;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A helper interface describing a type that can be a guard or a reference to a guard.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(GuardDescription.class),
    @JsonSubTypes.Type(GuardReferenceDescription.class)
})
public interface GuardOrGuardReferenceDescription {

}
