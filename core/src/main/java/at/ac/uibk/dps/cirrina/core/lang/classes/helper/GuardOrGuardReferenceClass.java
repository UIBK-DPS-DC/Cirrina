package at.ac.uibk.dps.cirrina.core.lang.classes.helper;

import at.ac.uibk.dps.cirrina.core.lang.classes.guard.GuardClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.guard.GuardReferenceClass;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A helper interface describing a type that can be a guard or a reference to a guard.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(GuardClass.class),
    @JsonSubTypes.Type(GuardReferenceClass.class)
})
public interface GuardOrGuardReferenceClass {

}
