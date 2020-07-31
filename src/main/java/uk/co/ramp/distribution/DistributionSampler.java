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

  public int resampleDays(int[] outcomes, double[] timeSpread) {
    return new EnumeratedIntegerDistribution(rng.getRandomGenerator(), outcomes, timeSpread)
        .sample();
  }
}
