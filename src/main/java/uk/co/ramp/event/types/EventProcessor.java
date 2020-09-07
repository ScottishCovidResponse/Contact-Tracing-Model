package uk.co.ramp.event.types;

import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.distribution.ImmutableBoundedDistribution;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.ImmutableDistribution;

public interface EventProcessor<T extends Event> {
  static BoundedDistribution scaleWithTimeSteps(BoundedDistribution distribution, int timeSteps) {

    double scale =
        distribution.distribution().internalScale().orElse(distribution.max()) * timeSteps;

    Distribution internalDistribution =
        ImmutableDistribution.builder()
            .from(distribution.distribution())
            .internalScale(scale)
            .rng(distribution.distribution().rng())
            .internalType(distribution.distribution().internalType())
            .build();

    return ImmutableBoundedDistribution.builder()
        .from(distribution)
        .max(distribution.max() * timeSteps)
        .distribution(internalDistribution)
        .build();
  }

  ProcessedEventResult processEvent(T event);
}
