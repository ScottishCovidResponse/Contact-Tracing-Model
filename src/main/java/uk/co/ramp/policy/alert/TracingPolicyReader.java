package uk.co.ramp.policy.alert;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import org.apache.commons.math3.random.RandomGenerator;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.policy.isolation.BoundedDistributionDeserializer;
import uk.co.ramp.policy.isolation.BoundedDistributionSerializer;
import uk.ramp.mapper.DataPipelineMapper;

class TracingPolicyReader {
  private final RandomGenerator rng;

  TracingPolicyReader(RandomGenerator rng) {
    this.rng = rng;
  }

  TracingPolicy read(Reader reader) {
    var boundedDistributionSerde =
        new SimpleModule()
            .addSerializer(BoundedDistribution.class, new BoundedDistributionSerializer(rng))
            .addDeserializer(BoundedDistribution.class, new BoundedDistributionDeserializer(rng));
    ObjectMapper objectMapper =
        new DataPipelineMapper(new JsonFactory(), rng).registerModule(boundedDistributionSerde);
    try {
      return objectMapper.readValue(reader, new TypeReference<ImmutableTracingPolicy>() {});
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
