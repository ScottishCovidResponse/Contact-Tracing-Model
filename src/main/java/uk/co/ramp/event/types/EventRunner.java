package uk.co.ramp.event.types;

public interface EventRunner {
    void run(int time, double randomInfectionRate, double randomCutOff);
}
