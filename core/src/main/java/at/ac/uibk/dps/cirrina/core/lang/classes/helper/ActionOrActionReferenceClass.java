package at.ac.uibk.dps.cirrina.core.lang.classes.helper;

import at.ac.uibk.dps.cirrina.core.lang.classes.action.ActionClass;
import at.ac.uibk.dps.cirrina.core.lang.classes.action.ActionReferenceClass;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(ActionClass.class),
    @JsonSubTypes.Type(ActionReferenceClass.class)
})
public interface ActionOrActionReferenceClass {

}
