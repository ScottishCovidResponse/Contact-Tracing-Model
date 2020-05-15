package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ProgressionDistribution;
import uk.co.ramp.people.InvalidStatusTransitionException;
import uk.co.ramp.people.Person;
import uk.co.ramp.people.VirusStatus;

import static uk.co.ramp.people.VirusStatus.*;

public class EvaluateCase {
    private static final Logger LOGGER = LogManager.getLogger(EvaluateCase.class);
    private final Person p;
    private final DiseaseProperties properties;
    private final RandomDataGenerator rng;

    public EvaluateCase(Person p, DiseaseProperties diseaseProperties, RandomDataGenerator randomDataGenerator) {
        this.p = p;
        this.properties = diseaseProperties;
        this.rng = randomDataGenerator;
    }


    public void updateStatus(VirusStatus newStatus, int currentTime, int exposedBy) {
        p.setExposedBy(exposedBy);
        updateStatus(newStatus, currentTime);
    }

    public void updateStatus(VirusStatus newStatus, int currentTime) {

        ProgressionDistribution progressionDistribution = properties.progressionDistribution();
        int timeToNextChange;
        double mean;
        double max;
        switch (newStatus) {
            case EXPOSED:
                mean = properties.meanTimeToInfectious();
                max = properties.maxTimeToInfectious();
                break;
            case EXPOSED_2:
                mean = properties.meanTimeToInfected();
                max = properties.maxTimeToInfected();
                break;
            case INFECTED:
            case INFECTED_SYMP:
                mean = properties.meanTimeToFinalState();
                max = properties.maxTimeToFinalState();
                break;
            default:
                mean = 0;
                max = 0;
        }

        if (newStatus != DEAD && newStatus != RECOVERED) {
            timeToNextChange = getDistributionValue(mean, max, progressionDistribution);
            p.setNextStatusChange(currentTime + timeToNextChange);
        } else {
            p.setNextStatusChange(-1);
        }

        p.setStatus(newStatus);

    }

    public void checkTime(int time) {

        if (p.getNextStatusChange() == time) {
            LOGGER.debug("Changing status for id: {}", p.getId());

            p.setNextStatusChange(-1);
            switch (p.getStatus()) {
                case EXPOSED:
                    updateStatus(EXPOSED_2, time);
                    break;
                case EXPOSED_2:
                    VirusStatus status = determineInfection(p);
                    updateStatus(status, time);
                    break;
                case INFECTED:
                    updateStatus(RECOVERED, time);
                    break;
                case INFECTED_SYMP:
                    status = determineOutcome(p);
                    updateStatus(status, time);
                    break;
                case SUSCEPTIBLE:
                case RECOVERED:
                case DEAD:
                    break;
            }

        } else if (p.getNextStatusChange() != -1 && p.getNextStatusChange() < time) {
            System.out.println("Something has been missed");
            System.out.println(p.toString());
            System.out.println(time);
            throw new RuntimeException("Something has been missed");
        }

    }

    private VirusStatus determineOutcome(Person p) {
        return p.getHealth() > 0.3 ? RECOVERED : DEAD;
    }

    private VirusStatus determineInfection(Person p) {
        return p.getHealth() > 0.5 ? INFECTED : INFECTED_SYMP;
    }

    public void randomExposure(int t) {
        p.setExposedBy(-1);
        if (p.getStatus() == SUSCEPTIBLE) {
            LOGGER.trace("Person with id: {} has been randomly exposed at time {}", p.getId(), t);
            updateStatus(EXPOSED, t);
        } else {
            String message = String.format("The person with id: %d should not be able to transition from %s to %s", p.getId(), p.getStatus(), EXPOSED);
            LOGGER.error(message);
            throw new InvalidStatusTransitionException(message);
        }
    }

    int getDistributionValue(double mean, double max, ProgressionDistribution p) {

        int value = (int) Math.round(mean);
        double sample;
        switch (p) {
            case GAUSSIAN:
                sample = rng.nextGaussian(mean, mean / 2d);
                break;
            case LINEAR:
                sample = rng.nextUniform(mean - mean / 2d, mean + mean / 2d);
                break;
            case EXPONENTIAL:
                sample = rng.nextExponential(mean);
                break;
            case FLAT:
            default:
                return value;
        }

        value = (int) Math.round(sample);

        return Math.min(Math.max(value, 1), 14);
    }


}
