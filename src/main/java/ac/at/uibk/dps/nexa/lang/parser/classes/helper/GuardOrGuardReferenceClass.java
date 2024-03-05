package ac.at.uibk.dps.nexa.lang.parser.classes.helper;

import ac.at.uibk.dps.nexa.lang.parser.classes.GuardClass;
import ac.at.uibk.dps.nexa.lang.parser.classes.GuardReferenceClass;
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
