package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.InvalidStatusTransitionException;
import uk.co.ramp.people.VirusStatus;

import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.VirusStatus.*;

public class EvaluateCase {
    private static final Logger LOGGER = LogManager.getLogger(EvaluateCase.class);
    private final Case p;
    private final DiseaseProperties properties;
    private final RandomDataGenerator rng;


    public EvaluateCase(Case p, DiseaseProperties diseaseProperties, RandomDataGenerator randomDataGenerator) {
        this.p = p;
        this.properties = diseaseProperties;
        this.rng = randomDataGenerator;

    }


    public void updateVirusStatus(VirusStatus newStatus, int currentTime, int exposedBy) {
        p.setExposedBy(exposedBy);
        updateVirusStatus(newStatus, currentTime);
    }

    public void updateVirusStatus(VirusStatus newStatus, int currentTime) {

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
            timeToNextChange = getDistributionValue(mean, max);
            p.setNextVirusStatusChange(currentTime + timeToNextChange);
        } else {
            p.setNextVirusStatusChange(-1);
        }

        p.setStatus(newStatus);

    }

    public void checkTime(int time) {

        if (p.nextVirusStatusChange() == time) checkVirusStatus(time);
        if (p.nextAlertStatusChange() == time) checkAlertStatus(time);


    }

    private void checkAlertStatus(int time) {
        LOGGER.debug("Changing alert status for id: {}", p.id());

        p.setNextAlertStatusChange(-1);
        switch (p.alertStatus()) {

            case TESTED_POSITIVE:
                LOGGER.warn("user has tested positive");
                alertAllContacts();
                break;
            case TESTED:
                LOGGER.warn("user has been tested");
                updateAlertStatus(TESTED_POSITIVE, time);
                break;
            case REQUESTED_TEST:
                LOGGER.warn("user has requested test");
                updateAlertStatus(TESTED, time);
                break;
            case ALERTED:
                LOGGER.warn("user has been alerted");
                break;
            case NONE:
                break;
        }


    }

    private void alertAllContacts() {
        //TODO
    }


    private void checkVirusStatus(int time) {

        LOGGER.debug("Changing virus status for id: {}", p.id());

        p.setNextVirusStatusChange(-1);
        switch (p.status()) {
            case EXPOSED:
                updateVirusStatus(EXPOSED_2, time);
                break;
            case EXPOSED_2:
                VirusStatus status = determineInfection(p);
                updateVirusStatus(status, time);
                break;
            case INFECTED:
                updateVirusStatus(RECOVERED, time);
                break;
            case INFECTED_SYMP:
                status = determineOutcome(p);
                updateAlertStatus(REQUESTED_TEST, time);
                updateVirusStatus(status, time);
                break;
            case SUSCEPTIBLE:
            case RECOVERED:
            case DEAD:
                break;
        }


    }

    private void updateAlertStatus(final AlertStatus newStatus, final int currentTime) {

        int time = getDistributionValue(properties.meanTestTime(), properties.maxTestTime());
        p.setAlertStatus(newStatus);
        p.setNextAlertStatusChange(currentTime + time);
        LOGGER.warn("id: {} alertStatus: {} at t: {}", p.id(), newStatus, currentTime);

    }

    private VirusStatus determineOutcome(Case p) {
        return p.health() > 0.3 ? RECOVERED : DEAD;
    }

    private VirusStatus determineInfection(Case p) {
        return p.health() > 0.5 ? INFECTED : INFECTED_SYMP;
    }

    public void randomExposure(int t) {
        p.setExposedBy(-1);
        if (p.status() == SUSCEPTIBLE) {
            LOGGER.trace("Person with id: {} has been randomly exposed at time {}", p.id(), t);
            updateVirusStatus(EXPOSED, t);
        } else {
            String message = String.format("The person with id: %d should not be able to transition from %s to %s", p.id(), p.status(), EXPOSED);
            LOGGER.error(message);
            throw new InvalidStatusTransitionException(message);
        }
    }

    int getDistributionValue(double mean, double max) {

        int value = (int) Math.round(mean);
        double sample;
        switch (properties.progressionDistribution()) {
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
