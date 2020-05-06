package uk.co.ramp.io;

public class DiseaseProperties {

    private final double meanTimeToInfected;
    private final double meanTimeToRecovered;
    private final double randomInfectionRate;
    private final ProgressionDistribution progressionDistribution;

    public DiseaseProperties(double meanTimeToInfected, double meanTimeToRecovered, double randomInfectionRate, ProgressionDistribution progressionDistribution) {
        this.meanTimeToInfected = meanTimeToInfected;
        this.meanTimeToRecovered = meanTimeToRecovered;
        this.progressionDistribution = progressionDistribution;
        this.randomInfectionRate = randomInfectionRate;
    }

    public double getMeanTimeToInfected() {
        return meanTimeToInfected;
    }

    public double getMeanTimeToRecovered() {
        return meanTimeToRecovered;
    }

    public double getRandomInfectionRate() {
        return randomInfectionRate;
    }

    public ProgressionDistribution getProgressionDistribution() {
        return progressionDistribution;
    }
}
