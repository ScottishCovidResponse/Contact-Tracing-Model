package uk.co.ramp.io;


public class DiseaseProperties {

    private final double meanTimeToInfectious;
    private final double meanTimeToInfected;
    private final double meanTimeToRecovered;
    private final double randomInfectionRate;
    private final double exposureTuning;
    private final ProgressionDistribution progressionDistribution;


    public DiseaseProperties(double meanTimeToInfectious, double meanTimeToInfected, double meanTimeToRecovered, double randomInfectionRate, double exposureTuning, ProgressionDistribution progressionDistribution) {
        this.meanTimeToInfectious = meanTimeToInfectious;
        this.meanTimeToInfected = meanTimeToInfected;
        this.meanTimeToRecovered = meanTimeToRecovered;
        this.progressionDistribution = progressionDistribution;
        this.randomInfectionRate = randomInfectionRate;
        this.exposureTuning = exposureTuning;
    }

    public double getMeanTimeToInfectious() {
        return meanTimeToInfectious;
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

    public double getExposureTuning() {
        return exposureTuning;
    }

    public ProgressionDistribution getProgressionDistribution() {
        return progressionDistribution;
    }
}
