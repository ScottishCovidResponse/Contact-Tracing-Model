package uk.co.ramp.io;

public class StandardProperties {

    private final int populationSize;
    private final int timeLimit;
    private final int infected;
    private int seed;
    private final boolean steadyState;

    public StandardProperties(int populationSize, int timeLimit, int infected, int seed, boolean steadyState) {
        this.populationSize = populationSize;
        this.timeLimit = timeLimit;
        this.infected = infected;
        this.seed = seed;
        this.steadyState = steadyState;
    }

    public int getSeed() {
        return seed;
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

    public void setSeed(int seed) {
        this.seed = seed;
    }

    @Override
    public String toString() {
        return "StandardProperties{" +
                "populationSize=" + populationSize +
                ", timeLimit=" + timeLimit +
                ", infected=" + infected +

                ", sid=" + seed +
                ", steadyState=" + steadyState +
                '}';
    }

    public boolean isSteadyState() {
        return steadyState;
    }
}
