package uk.co.ramp.distribution;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

public class DistributionSampler {
  private final RandomDataGenerator rng;

  public DistributionSampler(RandomDataGenerator rng) {
    this.rng = rng;
  }

  public double uniformBetweenZeroAndOne() {
    return rng.nextUniform(0, 1);
  }

  public int uniformInteger(int max) {
    return rng.nextInt(0, max);
  }

  public int getDistributionValue(Distribution distribution) {
    ProgressionDistribution distributionType = distribution.type();
    double mean = distribution.mean();
    double max = distribution.max();

    int value = (int) Math.round(mean);
    double sample;
    switch (distributionType) {
      case GAUSSIAN:
        sample = rng.nextGaussian(mean, mean / 2d);
        break;
      case LINEAR:
        sample = rng.nextUniform(mean - (max - mean), max);
        break;
      case EXPONENTIAL:
        sample = rng.nextExponential(mean);
        break;
      case FLAT:
      default:
        return value;
    }

    value = (int) Math.round(sample);

    // recursing to avoid artificial peaks at 1 and max
    if (value < 1 || value > max) {
      value = getDistributionValue(distribution);
    }

    return value;
  }

  public EnumeratedIntegerDistribution resampleDays(int[] outcomes, double[] timeSpread) {

    return new EnumeratedIntegerDistribution(rng.getRandomGenerator(), outcomes, timeSpread);
  }
}
