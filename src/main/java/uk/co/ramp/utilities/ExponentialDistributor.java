package uk.co.ramp.utilities;

public class ExponentialDistributor {


    public static double exponential(double random, double mean) {
        return -Math.log(random) * mean;
    }


}
