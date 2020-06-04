//package uk.co.ramp;
//
//import org.apache.commons.math3.random.RandomDataGenerator;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import uk.co.ramp.event.types.ContactEvent;
//import uk.co.ramp.io.types.DiseaseProperties;
//import uk.co.ramp.people.AlertStatus;
//import uk.co.ramp.people.Case;
//import uk.co.ramp.people.InvalidStatusTransitionException;
//import uk.co.ramp.people.VirusStatus;
//import uk.co.ramp.utilities.ForbiddenAccessException;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import static uk.co.ramp.people.AlertStatus.*;
//import static uk.co.ramp.people.VirusStatus.*;
//
//public class EvaluateCase {
//    private static final Logger LOGGER = LogManager.getLogger(EvaluateCase.class);
//    private final Case p;
//    private final DiseaseProperties properties;
//    private final RandomDataGenerator rng;
//
//
//    public EvaluateCase(Case p, DiseaseProperties diseaseProperties, RandomDataGenerator randomDataGenerator) {
//        this.p = p;
//        this.properties = diseaseProperties;
//        this.rng = randomDataGenerator;
//    }
//
//    public void initialExposure(VirusStatus newStatus, int currentTime) {
//        if (currentTime == 0) {
//            updateVirusStatus(newStatus, Case.getDefault(), Case.getInitial());
//        } else {
//            String message = "Unable to set an initial exposure at t > 0";
//            LOGGER.error(message);
//            throw new ForbiddenAccessException(message);
//        }
//    }
//
//    public void updateVirusStatus(VirusStatus newStatus, int currentTime, int exposedBy) {
//        updateVirusStatus(newStatus, currentTime);
//    }
//
//    private void updateVirusStatus(VirusStatus newStatus, int currentTime) {
//
//        int timeToNextChange;
//        double mean;
//        double max;
//        switch (newStatus) {
//            case EXPOSED:
//                mean = properties.meanTimeToInfectious();
//                max = properties.maxTimeToInfectious();
//                break;
//            case PRESYMPTOMATIC:
//                mean = properties.meanTimeToInfected();
//                max = properties.maxTimeToInfected();
//                break;
//            default:
//                mean = properties.meanTimeToFinalState();
//                max = properties.maxTimeToFinalState();
//        }
//
//        if (newStatus != DEAD && newStatus != RECOVERED) {
//            timeToNextChange = getDistributionValue(mean, max);
//            p.setNextVirusStatusChange(currentTime + timeToNextChange);
//        } else {
//            p.setNextVirusStatusChange(Case.getDefault());
//        }
//
////        p.setVirusStatus(newStatus);
//
//    }
//
//    public Set<Integer> checkActionsAtTimestep(int time) {
//        Set<Integer> alerts = new HashSet<>();
//        if (p.nextVirusStatusChange() == time) checkUpdateVirusStatus(time);
//        if (p.nextAlertStatusChange() == time) alerts = checkUpdateAlertStatus(time);
//
//        return alerts;
//    }
//
//    private void checkUpdateVirusStatus(int time) {
//
////        p.setNextVirusStatusChange(-1);
////        switch (p.status()) {
////            case EXPOSED:
////                updateVirusStatus(EXPOSED_2, time);
////                break;
////            case EXPOSED_2:
////                VirusStatus status = determineInfection(p);
////                updateVirusStatus(status, time);
////                if (status == INFECTED_SYMP && p.alertStatus() == NONE) updateAlertStatus(REQUESTED_TEST, time);
////                break;
////            case INFECTED:
////                updateVirusStatus(RECOVERED, time);
////                break;
////            case INFECTED_SYMP:
////                status = determineOutcome(p);
////                updateVirusStatus(status, time);
////                break;
////            case SUSCEPTIBLE:
////            case RECOVERED:
////            case DEAD:
////                break;
////        }
//    }
//
//
//    private Set<Integer> checkUpdateAlertStatus(int time) {
//        LOGGER.trace("Changing alert status for id: {}", p.id());
//
//        p.setNextAlertStatusChange(-1);
//        switch (p.alertStatus()) {
//
//            case TESTED_POSITIVE:
//                // TODO: at present the patient doesn't move from here.. should they go to immune after some time?
//                LOGGER.trace("user {} has tested positive", p.id());
//                return alertAllContacts();
//            case AWAITING_RESULT:
//                // TODO: maybe include flag for has had virus?
//                // TODO: should a recovered person test +ve?
//                if (p.wasInfectiousWhenTested()) {
//                    LOGGER.trace("user {} has tested positive", p.id());
//                    updateAlertStatus(TESTED_POSITIVE, time);
//                } else {
//                    LOGGER.trace("user {} has tested negative", p.id());
//                    updateAlertStatus(NONE, time);
//                }
//
//                break;
//            case REQUESTED_TEST:
//                LOGGER.trace("user {} is awaiting test result", p.id());
//                updateAlertStatus(AWAITING_RESULT, time);
//                p.setWasInfectiousWhenTested(p.isInfectious());
//                break;
//            case ALERTED:
//                updateAlertStatus(REQUESTED_TEST, time);
//                LOGGER.trace("user {} has requested a test. Current Status: {}-{}", p.id(), p.status(), p.alertStatus());
//                break;
//            case NONE:
//                LOGGER.trace("user {} has been alerted. Current Status: {}-{}", p.id(), p.status(), p.alertStatus());
//                updateAlertStatus(ALERTED, time);
//                break;
//        }
//
//        return new HashSet<>();
//    }
//
//    private Set<Integer> alertAllContacts() {
//
//        Set<Integer> contactIds = new HashSet<>();
//        // add all ids
//        for (ContactEvent r : p.contactRecords()) {
//            contactIds.add(r.from());
//            contactIds.add(r.to());
//        }
//
//        // remove self
//        contactIds.remove(p.id());
//
//        LOGGER.trace("Alerting {}", contactIds);
//        return contactIds;
//
//    }
//
//
//    private void updateAlertStatus(final AlertStatus newStatus, final int currentTime) {
//
//        int time = getDistributionValue(properties.meanTestTime(), properties.maxTestTime());
//        AlertStatus oldstatus = p.alertStatus();
////        p.setAlertStatus(newStatus);
//        p.setNextAlertStatusChange(currentTime + time);
//        LOGGER.trace("id: {} alertStatus: {} -> {} at t={}. Current Virus status: {}", p.id(), oldstatus, newStatus, currentTime, p.status());
//
//    }
//
//    private VirusStatus determineOutcome(Case p) {
//        //TODO add real logic
//        return p.health() > rng.nextUniform(0, 1) ? RECOVERED : DEAD;
//    }
//
//    private VirusStatus determineInfection(Case p) {
//        //TODO add real logic
//        return p.health() > rng.nextUniform(0, 1) ? ASYMPTOMATIC : SYMPTOMATIC;
//    }
//
//    public void randomExposure(int t) {
////        p.setExposedBy(-2);
////        p.setExposedTime(t);
//        if (p.status() == SUSCEPTIBLE) {
//            LOGGER.trace("Person with id: {} has been randomly exposed at time {}", p.id(), t);
//            updateVirusStatus(EXPOSED, t);
//        } else {
//            String message = String.format("The person with id: %d should not be able to transition from %s to %s", p.id(), p.status(), EXPOSED);
//            LOGGER.error(message);
//            throw new InvalidStatusTransitionException(message);
//        }
//    }
//
//    int getDistributionValue(double mean, double max) {
//
//        int value = (int) Math.round(mean);
//        double sample;
//        switch (properties.progressionDistribution()) {
//            case GAUSSIAN:
//                sample = rng.nextGaussian(mean, mean / 2d);
//                break;
//            case LINEAR:
//                sample = rng.nextUniform(mean - mean / 2d, mean + mean / 2d);
//                break;
//            case EXPONENTIAL:
//                sample = rng.nextExponential(mean);
//                break;
//            case FLAT:
//            default:
//                return value;
//        }
//
//        value = (int) Math.round(sample);
//
//        return Math.min(Math.max(value, 1), (int) max);
//    }
//
//
//}

