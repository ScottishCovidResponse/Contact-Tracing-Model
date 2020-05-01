package uk.co.ramp.io;

import uk.co.ramp.utilities.MinMax;

import java.util.Map;

public class PopulationProperties {

    private final Map<Integer, Double> populationDistribution;
    private final Map<Integer, MinMax> populationAges;
    private final double genderBalance;

    public PopulationProperties(Map<Integer, Double> populationDistribution, Map<Integer, MinMax> populationAges, double genderBalance) {
        this.populationAges = populationAges;
        this.populationDistribution = populationDistribution;
        this.genderBalance = genderBalance;
    }

    public Map<Integer, Double> getPopulationDistribution() {
        return populationDistribution;
    }

    public Map<Integer, MinMax> getPopulationAges() {
        return populationAges;
    }

    public double getGenderBalance() {
        return genderBalance;
    }
}
