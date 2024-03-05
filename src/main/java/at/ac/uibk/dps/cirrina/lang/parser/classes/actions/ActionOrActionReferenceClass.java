package at.ac.uibk.dps.cirrina.lang.parser.classes.actions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(ActionClass.class),
    @JsonSubTypes.Type(ActionReferenceClass.class)
})
public interface ActionOrActionReferenceClass {

}
