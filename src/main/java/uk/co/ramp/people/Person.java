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

    private int nextStatusChange = -1;


    public Person(final int id, int age, Gender gender, double compliance, double health) {
        this.id = id;
        this.age = age;
        this.gender = gender;
        this.compliance = compliance;
        this.health = health;
        this.status = SUSCEPTIBLE;
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

    private void setStatus(VirusStatus status) {
        this.status = status;
    }

    public void updateStatus(VirusStatus newStatus, int currentTime) {
        if (newStatus == EXPOSED) {
            setStatus(newStatus);
            nextStatusChange = currentTime + 3;
        } else if (newStatus == INFECTED) {
            nextStatusChange = currentTime + 7;
            setStatus(INFECTED);
        } else if (newStatus == RECOVERED) {
            setStatus(RECOVERED);
        }
    }

    public int getNextStatusChange() {
        return nextStatusChange;
    }

    public void checkTime(int time) {

        if (nextStatusChange == time) {
            LOGGER.info("Changing status for id: {}", id);

            nextStatusChange = -1;

            if (status == EXPOSED) {
                updateStatus(INFECTED, time);
            } else if (status == INFECTED) {
                updateStatus(RECOVERED, time);
            } else {
                throw new RuntimeException("Shouldn't get here?");
            }
        }

    }
}
