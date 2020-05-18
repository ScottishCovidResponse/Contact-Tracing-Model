package uk.co.ramp.people;

import uk.co.ramp.contact.ContactRecord;

import java.util.HashSet;
import java.util.Set;

import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.VirusStatus.*;

public class Case {

    private final Human human;
    private final Set<ContactRecord> contactRecords;
    private VirusStatus status;
    private AlertStatus alertStatus;
    private int exposedBy;
    private int nextVirusStatusChange;
    private int nextAlertStatusChange;

    public Case(final Human human) {
        this.human = human;
        exposedBy = -1;
        nextVirusStatusChange = -1;
        status = SUSCEPTIBLE;
        contactRecords = new HashSet<>();
        alertStatus = NONE;
    }

    public int nextAlertStatusChange() {
        return nextAlertStatusChange;
    }

    public void setNextAlertStatusChange(int nextAlertStatusChange) {
        this.nextAlertStatusChange = nextAlertStatusChange;
    }

    public AlertStatus alertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = alertStatus;
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
        return status == INFECTED || status == INFECTED_SYMP || status == EXPOSED_2;
    }

    public Human getHuman() {
        return human;
    }

    public VirusStatus status() {
        return status;
    }

    public void setStatus(VirusStatus status) {
        this.status = this.status.transitionTo(status);
    }

    public int exposedBy() {
        return exposedBy;
    }

    public void setExposedBy(int exposedBy) {
        this.exposedBy = exposedBy;
    }

    public int nextVirusStatusChange() {
        return nextVirusStatusChange;
    }

    public void setNextVirusStatusChange(int nextVirusStatusChange) {
        this.nextVirusStatusChange = nextVirusStatusChange;
    }

    public void addContact(ContactRecord record) {
        contactRecords.add(record);
    }

}
