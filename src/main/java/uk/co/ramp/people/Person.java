package uk.co.ramp.people;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.ContactRunner;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ProgressionDistribution;

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

                String message = String.format("Changing status from %s for person.id %d is not a valid transition", status, id);
                LOGGER.error(message);
                throw new InvalidStatusTransitionException(message);
            }
        }

    }

    public void randomExposure(int t) {
        exposedBy = -1;
        if (status == SUSCEPTIBLE) {
            LOGGER.info("Person with id: {} has been randomly exposed at time {}", getId(), t);
            updateStatus(EXPOSED, t);
        } else {
            String message = String.format("The person with id: %d should not be able to transition from %s to %s", id, status, EXPOSED);
            LOGGER.error(message);
            throw new InvalidStatusTransitionException(message);
        }
    }

    int getDistributionValue(double mean, ProgressionDistribution p) {
        RandomDataGenerator rnd = ContactRunner.getRng();

        int value = (int) Math.round(mean);
        double sample;
        switch (p) {
            case GAUSSIAN:
                sample = rnd.nextGaussian(mean, mean / 2d);
                break;
            case LINEAR:
                sample = rnd.nextUniform(mean - mean / 2d, mean + mean / 2d);
                break;
            case EXPONENTIAL:
                sample = rnd.nextExponential(mean);
                break;
            case FLAT:
            default:
                return value;
        }

        value = (int) Math.round(sample);

        return Math.min(Math.max(value, 1), 14);
    }


}
