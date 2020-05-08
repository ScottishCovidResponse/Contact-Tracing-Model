package uk.co.ramp.people;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.ContactRunner;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ProgressionDistribution;
import uk.co.ramp.utilities.ExponentialDistributor;

import java.util.Random;

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

    private void setStatus(VirusStatus status) {
        this.status = status;
    }

    public void updateStatus(VirusStatus newStatus, int currentTime, int exposedBy) {
        this.exposedBy = exposedBy;
        updateStatus(newStatus, currentTime);
    }

    public void updateStatus(VirusStatus newStatus, int currentTime) {

        DiseaseProperties properties = ContactRunner.getDiseaseProperties();

        if (newStatus == EXPOSED) {
            setStatus(newStatus);
            nextStatusChange = currentTime + getDistributionValue(properties.getMeanTimeToInfected(), properties.getProgressionDistribution());
        } else if (newStatus == INFECTED) {
            nextStatusChange = currentTime + getDistributionValue(properties.getMeanTimeToRecovered(), properties.getProgressionDistribution());
            setStatus(INFECTED);
        } else if (newStatus == RECOVERED) {
            setStatus(RECOVERED);
            nextStatusChange = -1;
        }
    }

    public int getNextStatusChange() {
        return nextStatusChange;
    }

    public void checkTime(int time) {

        if (nextStatusChange == time) {
            LOGGER.debug("Changing status for id: {}", id);

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

    public void randomExposure(int t) {
        exposedBy = -1;
        if (status == SUSCEPTIBLE) {
            LOGGER.info("Person with id: {} has been randomly exposed at time {}", getId(), t);
            updateStatus(EXPOSED, t);
        }
    }

    int getDistributionValue(double mean, ProgressionDistribution p) {

        Random rng = ContactRunner.getRng();


        int value = (int) mean;
        double sample;
        switch (p) {
            case GAUSSIAN:
                sample = rng.nextGaussian() + mean;
                break;
            case LINEAR:
                sample = rng.nextDouble() * 2d * mean;
                break;
            case EXPONENTIAL:
                sample = ExponentialDistributor.exponential(rng.nextDouble(), mean);
                break;
            case FLAT:
            default:
                return value;
        }


        value = (int) sample;

        return Math.min(Math.max(value, 1), 14);
    }


}
