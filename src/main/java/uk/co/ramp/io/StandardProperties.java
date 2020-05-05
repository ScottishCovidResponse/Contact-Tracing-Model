package uk.co.ramp.io;

public class StandardProperties {

    private final int populationSize;
    private final int timeLimit;
    private final int infected;
    private final double randomInfectionRate;
    private final int sid;
    private final boolean steadyState;

    public StandardProperties(int populationSize, int timeLimit, int infected, double randomInfectionRate, int sid, boolean steadyState) {
        this.populationSize = populationSize;
        this.timeLimit = timeLimit;
        this.infected = infected;
        this.sid = sid;
        this.steadyState = steadyState;
        this.randomInfectionRate = randomInfectionRate;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getInfected() {
        return infected;
    }

    public int getSid() {
        return sid;
    }

    public double getRandomInfectionRate() {
        return randomInfectionRate;
    }

    @Override
    public String toString() {
        return "StandardProperties{" +
                "populationSize=" + populationSize +
                ", timeLimit=" + timeLimit +
                ", infected=" + infected +
                ", randomInfectionRate=" + randomInfectionRate +
                ", sid=" + sid +
                ", steadyState=" + steadyState +
                '}';
    }

    public boolean isSteadyState() {
        return steadyState;
    }
}
