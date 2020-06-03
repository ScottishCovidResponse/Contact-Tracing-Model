package uk.co.ramp.people;

import com.google.common.base.Strings;
import uk.co.ramp.event.*;

import java.util.HashSet;
import java.util.Set;

import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.VirusStatus.*;

public class Case {

    private final Human human;
    private final Set<ContactEvent> contactRecords;
    private VirusStatus virusStatus;
    private AlertStatus alertStatus;
    private int exposedBy;
    private int exposedTime;
    private int nextVirusStatusChange;
    private int nextAlertStatusChange;
    private boolean wasInfectiousWhenTested;

    private static final int DEFAULT = -1;
    private static final int INITIAL = -3;
    private static final int RANDOM_INFECTION = -2;

    public Case(final Human human) {
        this.human = human;
        virusStatus = SUSCEPTIBLE;
        alertStatus = NONE;
        contactRecords = new HashSet<>();
        exposedBy = DEFAULT;
        exposedTime = -1;
        nextVirusStatusChange = -1;
        nextAlertStatusChange = -1;
        wasInfectiousWhenTested = false;
    }

    public static int getDefault() {
        return DEFAULT;
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

    public double compliance() {
        return human.compliance();
    }

    public Gender gender() {
        return human.gender();
    }

    public boolean isInfectious() {
        return virusStatus == ASYMPTOMATIC || virusStatus == SYMPTOMATIC || virusStatus == PRESYMPTOMATIC;
    }

    public Human getHuman() {
        return human;
    }

    public VirusStatus status() {
        return virusStatus;
    }

    public static int getInitial() {
        return INITIAL;
    }

    public int age() {
        return getHuman().age();
    }

    // Event statuses

    public int nextVirusStatusChange() {
        return nextVirusStatusChange;
    }

    public void setNextVirusStatusChange(int nextVirusStatusChange) {
        this.nextVirusStatusChange = nextVirusStatusChange;
    }

    public boolean wasInfectiousWhenTested() {
        return wasInfectiousWhenTested;
    }

    public void setWasInfectiousWhenTested(boolean wasInfectiousWhenTested) {
        this.wasInfectiousWhenTested = wasInfectiousWhenTested;
    }

    protected void setVirusStatus(VirusStatus newStatus) {
        this.virusStatus = this.virusStatus.transitionTo(newStatus);
    }

    public int nextAlertStatusChange() {
        return nextAlertStatusChange;
    }


    // Exposure and contact data

    public void setNextAlertStatusChange(int nextAlertStatusChange) {
        this.nextAlertStatusChange = nextAlertStatusChange;
    }

    public void addContact(ContactEvent record) {
        contactRecords.add(record);
    }

    public AlertStatus alertStatus() {
        return alertStatus;
    }

    protected void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = this.alertStatus.transitionTo(alertStatus);
    }

    public Set<ContactEvent> contactRecords() {
        return contactRecords;
    }

    public int exposedBy() {
        return exposedBy;
    }

    protected void setExposedBy(int exposedBy) {
        this.exposedBy = exposedBy;
    }

    // statics

    public int exposedTime() {
        return exposedTime;
    }

    protected void setExposedTime(int exposedTime) {
        this.exposedTime = exposedTime;
    }

    public String getSource() {
        return Strings.padEnd(id() + "(" + exposedTime + ")", 12, ' ');
    }

    protected void processInfectionEvent(InfectionEvent event, int time) {

        setVirusStatus(event.newStatus());
        setExposedTime(event.exposedTime());
        setExposedBy(event.exposedBy());

    }


    protected void processVirusEvent(VirusEvent event, int time) {
        setVirusStatus(event.newStatus());
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
        setAlertStatus(event.newStatus());
    }


}
