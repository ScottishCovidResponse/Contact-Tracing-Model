package uk.co.ramp.people;

import com.google.common.base.Strings;
import uk.co.ramp.event.types.AlertEvent;
import uk.co.ramp.event.types.Event;
import uk.co.ramp.event.types.InfectionEvent;
import uk.co.ramp.event.types.VirusEvent;

import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.VirusStatus.*;

public class Case {

    private final Human human;
    private VirusStatus virusStatus;
    private AlertStatus alertStatus;
    private int exposedBy;
    private int exposedTime;

    private static final int DEFAULT = -1;
    private static final int INITIAL = -3;
    private static final int RANDOM_INFECTION = -2;

    public Case(final Human human) {
        this.human = human;
        virusStatus = SUSCEPTIBLE;
        alertStatus = NONE;
        exposedBy = DEFAULT;
        exposedTime = -1;
    }


    // simple getters

    public Human getHuman() {
        return human;
    }

    public VirusStatus virusStatus() {
        return virusStatus;
    }

    public AlertStatus alertStatus() {
        return alertStatus;
    }

    public int exposedBy() {
        return exposedBy;
    }

    public int exposedTime() {
        return exposedTime;
    }


    // exposing from Human Type

    // statics
    public static int getDefault() {
        return DEFAULT;
    }

    public static int getInitial() {
        return INITIAL;
    }

    public static int getRandomInfection() {
        return RANDOM_INFECTION;
    }

    public double health() {
        return human.health();
    }

    public int id() {
        return human.id();
    }


    //derived values

    public String getSource() {
        return Strings.padEnd(id() + "(" + exposedTime + ")", 12, ' ');
    }

    public double compliance() {
        return human.compliance();
    }

    public Gender gender() {
        return human.gender();
    }

    public int age() {
        return human.age();
    }

    public boolean isInfectious() {
        return virusStatus == ASYMPTOMATIC || virusStatus == SYMPTOMATIC || virusStatus == SEVERELY_SYMPTOMATIC || virusStatus == PRESYMPTOMATIC;
    }

    // Event handling
    private void processInfectionEvent(InfectionEvent event, int time) {
        virusStatus = virusStatus.transitionTo(event.newStatus());
        exposedTime = event.exposedTime();
        exposedBy = event.exposedBy();
    }

    private void processVirusEvent(VirusEvent event, int time) {
        virusStatus = virusStatus.transitionTo(event.newStatus());
    }

    public void processEvent(Event event, int time) {

        if (event instanceof VirusEvent) {
            processVirusEvent((VirusEvent) event, time);
        } else if (event instanceof InfectionEvent) {
            processInfectionEvent((InfectionEvent) event, time);
        } else if (event instanceof AlertEvent) {
            processAlertEvent((AlertEvent) event, time);
        } else {
            throw new RuntimeException("uncovered condition");
        }

    }

    private void processAlertEvent(AlertEvent event, int time) {
        alertStatus = event.newStatus();
    }


}
