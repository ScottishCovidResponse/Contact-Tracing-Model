package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ProgressionDistribution;
import uk.co.ramp.people.InvalidStatusTransitionException;
import uk.co.ramp.people.ImmutablePerson;
import uk.co.ramp.people.Person;
import uk.co.ramp.people.VirusStatus;

import static uk.co.ramp.people.VirusStatus.*;

public class EvaluateCase {
    private static final Logger LOGGER = LogManager.getLogger(EvaluateCase.class);
    private final DiseaseProperties properties;
    private final RandomDataGenerator rng;

    public EvaluateCase(DiseaseProperties diseaseProperties, RandomDataGenerator randomDataGenerator) {
        this.properties = diseaseProperties;
        this.rng = randomDataGenerator;
    }


    public Person updateStatus(Person p, VirusStatus newStatus, int currentTime, int exposedBy) {
        p = ImmutablePerson.copyOf(p).withExposedBy(exposedBy);
        return updateStatus(p, newStatus, currentTime);
    }

    public Person updateStatus(Person p, VirusStatus newStatus, int currentTime) {

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
            p = ImmutablePerson.copyOf(p).withNextStatusChange(currentTime + timeToNextChange);
        } else {
            p = ImmutablePerson.copyOf(p).withNextStatusChange(-1);
        }

        if (p.status().isValidTransition(newStatus)) {
            return ImmutablePerson.copyOf(p).withStatus(newStatus);
        } else {
            String errorMsg = String.format("It is not valid to transition between statuses %s -> %s", p.status(), newStatus);
            LOGGER.error(errorMsg);
            throw new InvalidStatusTransitionException(errorMsg);
        }
    }

    public Person checkTime(Person p, int time) {

        if (p.nextStatusChange() == time) {
            LOGGER.debug("Changing status for id: {}", p.id());

            p = ImmutablePerson.copyOf(p).withNextStatusChange(-1);
            switch (p.status()) {
                case EXPOSED:
                    return updateStatus(p, EXPOSED_2, time);
                case EXPOSED_2:
                    VirusStatus status = determineInfection(p);
                    return updateStatus(p, status, time);
                case INFECTED:
                    return updateStatus(p, RECOVERED, time);
                case INFECTED_SYMP:
                    status = determineOutcome(p);
                    return updateStatus(p, status, time);
                case SUSCEPTIBLE:
                case RECOVERED:
                case DEAD:
                    break;
            }

        } else if (p.nextStatusChange() != -1 && p.nextStatusChange() < time) {
            throw new RuntimeException("Something has been missed");
        }
        return p;
    }

    private VirusStatus determineOutcome(Person p) {
        return p.health() > 0.3 ? RECOVERED : DEAD;
    }

    private VirusStatus determineInfection(Person p) {
        return p.health() > 0.5 ? INFECTED : INFECTED_SYMP;
    }

    public Person randomExposure(Person p, int t) {
        p = ImmutablePerson.copyOf(p).withExposedBy(-1);
        if (p.status() == SUSCEPTIBLE) {
            LOGGER.trace("Person with id: {} has been randomly exposed at time {}", p.id(), t);
            return updateStatus(p, EXPOSED, t);
        } else {
            String message = String.format("The person with id: %d should not be able to transition from %s to %s", p.id(), p.status(), EXPOSED);
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

        return Math.min(Math.max(value, 1), (int) max);
    }


}
