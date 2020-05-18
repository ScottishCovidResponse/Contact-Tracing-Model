package uk.co.ramp.people;

import com.google.common.base.Preconditions;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import static uk.co.ramp.people.VirusStatus.*;

@Gson.TypeAdapters
@Value.Modifiable
//@Value.Style(defaultAsDefault = true)
public interface Person {

    int id();

    int age();

    Gender gender();

    double compliance();

    double health();

    VirusStatus status();

    int exposedBy();

    int nextStatusChange();

    default boolean isInfectious() {
        return status() == INFECTED || status() == INFECTED_SYMP || status() == EXPOSED_2;
    }


    default ModifiablePerson setStatus(VirusStatus next) {
        if (status().getValidTransitions().contains(next)) {
            System.out.println("This");
        } else {
            System.out.println("That");
        }
        Preconditions.checkState(status().getValidTransitions().contains(next), "Invalid transition");
        return (ModifiablePerson) this;
    }


}
