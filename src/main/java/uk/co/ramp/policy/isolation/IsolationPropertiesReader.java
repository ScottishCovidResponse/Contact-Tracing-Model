package uk.co.ramp.policy.isolation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import org.apache.commons.math3.random.RandomGenerator;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.ramp.mapper.DataPipelineMapper;

class IsolationPropertiesReader {
  private final RandomGenerator rng;

  IsolationPropertiesReader(RandomGenerator rng) {
    this.rng = rng;
  }

  IsolationProperties read(Reader reader) {
    var boundedDistributionSerde =
        new SimpleModule()
            .addSerializer(BoundedDistribution.class, new BoundedDistributionSerializer(rng))
            .addDeserializer(BoundedDistribution.class, new BoundedDistributionDeserializer(rng));
    ObjectMapper objectMapper =
        new DataPipelineMapper(new JsonFactory(), rng).registerModule(boundedDistributionSerde);
    try {
      return objectMapper.readValue(reader, new TypeReference<ImmutableIsolationProperties>() {});
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
