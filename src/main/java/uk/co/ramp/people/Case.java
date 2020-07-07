package uk.co.ramp.people;

import static uk.co.ramp.people.AlertStatus.NONE;
import static uk.co.ramp.people.VirusStatus.*;

public class Case {

  private final Human human;
  private VirusStatus virusStatus;
  private AlertStatus alertStatus;
  private int exposedBy;
  private int exposedTime;

  private static final int DEFAULT = -1;
  private static final int INITIAL = -3;
  private static final int RANDOM_INFECTION = -2;

  public Case(final Human human) {
    this.human = human;
    virusStatus = SUSCEPTIBLE;
    alertStatus = NONE;
    exposedBy = DEFAULT;
    exposedTime = -1;
  }

  // simple get/setters

  public Human getHuman() {
    return human;
  }

  public VirusStatus virusStatus() {
    return virusStatus;
  }

  public AlertStatus alertStatus() {
    return alertStatus;
  }

  public int exposedBy() {
    return exposedBy;
  }

  public int exposedTime() {
    return exposedTime;
  }

  public void setVirusStatus(VirusStatus virusStatus) {
    this.virusStatus = this.virusStatus.transitionTo(virusStatus);
  }

  public void setAlertStatus(AlertStatus alertStatus) {
    this.alertStatus = this.alertStatus.transitionTo(alertStatus);
  }

  // exposing from Human Type

  // statics
  public static int getDefault() {
    return DEFAULT;
  }

  public static int getInitial() {
    return INITIAL;
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

  // derived values

  public double compliance() {
    return human.isolationCompliance();
  }

  public Gender gender() {
    return human.gender();
  }

  public int age() {
    return human.age();
  }

  public boolean isInfectious() {
    return virusStatus == ASYMPTOMATIC
        || virusStatus == SYMPTOMATIC
        || virusStatus == SEVERELY_SYMPTOMATIC
        || virusStatus == PRESYMPTOMATIC;
  }

  public void setExposedBy(int exposedBy) {
    this.exposedBy = exposedBy;
  }

  public void setExposedTime(int exposedTime) {
    this.exposedTime = exposedTime;
  }

  //    // Event handling
  //    private void processInfectionEvent(InfectionEvent event) {
  //        virusStatus = virusStatus.transitionTo(event.nextStatus());
  //        exposedTime = event.exposedTime();
  //        exposedBy = event.exposedBy();
  //    }
  //
  //    private void processVirusEvent(VirusEvent event) {
  //        virusStatus = virusStatus.transitionTo(event.nextStatus());
  //    }
  //
  //    public void processEvent(Event event) {
  //
  //        if (event instanceof VirusEvent) {
  //            processVirusEvent((VirusEvent) event);
  //        } else if (event instanceof InfectionEvent) {
  //            processInfectionEvent((InfectionEvent) event);
  //        } else if (event instanceof AlertEvent) {
  //            processAlertEvent((AlertEvent) event);
  //        } else {
  //            throw new EventException("uncovered condition");
  //        }
  //
  //    }

  //    private void processAlertEvent(AlertEvent event) {
  //        alertStatus = event.nextStatus();
  //    }

}
