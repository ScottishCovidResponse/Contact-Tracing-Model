package uk.co.ramp.event;

import uk.co.ramp.event.types.*;
import uk.co.ramp.people.Case;

public class FormattedEventFactory {

    public static ImmutableFormattedEvent create(VirusEvent event) {
        return ImmutableFormattedEvent.builder().
                time(event.time()).
                eventType("VirusEvent").
                id(event.id()).
                newStatus(event.newStatus().toString()).
                additionalInfo("").
                build();
    }


    public static ImmutableFormattedEvent create(InfectionEvent event) {

        String exposedBy;
        if (event.exposedBy() == Case.getInitial()) {
            exposedBy = "This case was an initial infection";
        } else if (event.exposedBy() == Case.getRandomInfection()) {
            exposedBy = "This case was a random infection";
        } else {
            exposedBy = "This case was due to contact with " + event.exposedBy();
        }


        return ImmutableFormattedEvent.builder().
                time(event.time()).
                eventType("InfectionEvent").
                id(event.id()).
                newStatus(event.newStatus().toString()).
                additionalInfo(exposedBy).
                build();
    }

    public static ImmutableFormattedEvent create(AlertEvent event) {
        return ImmutableFormattedEvent.builder().
                time(event.time()).
                eventType("AlertEvent").
                id(event.id()).
                newStatus(event.newStatus().toString()).
                additionalInfo("").
                build();
    }


    public static ImmutableFormattedEvent create(PolicyEvent event) {
        return ImmutableFormattedEvent.builder().
                time(event.time()).
                eventType("PolicyEvent").
                id(0).
                newStatus("").
                additionalInfo("").
                build();
    }


    public static ImmutableFormattedEvent create(ContactEvent event) {
        return ImmutableFormattedEvent.builder().
                time(event.time()).
                eventType("ContactEvent").
                id(event.to()).
                newStatus("").
                additionalInfo("").
                build();
    }

    public static ImmutableFormattedEvent create(Event event) {

        if (event instanceof VirusEvent) {
            return create((VirusEvent) event);
        } else if (event instanceof InfectionEvent) {
            return create((InfectionEvent) event);
        } else if (event instanceof AlertEvent) {
            return create((AlertEvent) event);
        }
        return null;
    }
}
