package at.ac.uibk.dps.cirrina.csml.description.helper;

import at.ac.uibk.dps.cirrina.csml.description.action.ActionDescription;
import at.ac.uibk.dps.cirrina.csml.description.action.ActionReferenceDescription;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(ActionDescription.class),
    @JsonSubTypes.Type(ActionReferenceDescription.class)
})
public interface ActionOrActionReferenceDescription {

}
