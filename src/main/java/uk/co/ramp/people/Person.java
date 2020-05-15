package uk.co.ramp.people;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static uk.co.ramp.people.VirusStatus.*;

public class Person {

    private static final Logger LOGGER = LogManager.getLogger(Person.class);

    // key characteristics
    private final int id;
    private final int age;
    private final Gender gender;
    private final double compliance;
    private final double health;
    private VirusStatus status;
    private int exposedBy;

    private int nextStatusChange = -1;


    public Person(final int id, int age, Gender gender, double compliance, double health) {
        this.id = id;
        this.age = age;
        this.gender = gender;
        this.compliance = compliance;
        this.health = health;
        this.status = SUSCEPTIBLE;
        this.exposedBy = -1;
    }


    public int getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public double getCompliance() {
        return compliance;
    }

    public double getHealth() {
        return health;
    }

    public VirusStatus getStatus() {
        return status;
    }

    public int getExposedBy() {
        return exposedBy;
    }

    public void setStatus(VirusStatus next) {
        status = status.transitionTo(next);
    }

    public void setExposedBy(int exposedBy) {
        this.exposedBy = exposedBy;
    }


    public int getNextStatusChange() {
        return nextStatusChange;
    }

    public void setNextStatusChange(int nextStatusChange) {
        this.nextStatusChange = nextStatusChange;
    }

    public boolean isInfectious() {
        return getStatus() == INFECTED || getStatus() == INFECTED_SYMP || getStatus() == EXPOSED_2;
    }

}
