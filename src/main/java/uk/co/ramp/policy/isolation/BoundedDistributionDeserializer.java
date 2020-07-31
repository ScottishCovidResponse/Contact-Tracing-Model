package uk.co.ramp.policy.isolation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.apache.commons.math3.random.RandomGenerator;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.distribution.ImmutableBoundedDistribution;
import uk.ramp.distribution.ImmutableDistribution;
import uk.ramp.mapper.DataPipelineMapper;

/**
 * This class provides a temporary deserialization wrapper over the distribution from std api. This
 * can/will be removed once we migrate ALL 'distribution' related objects to load from the data
 * pipeline.
 */
public class BoundedDistributionDeserializer extends JsonDeserializer<BoundedDistribution> {
  private final RandomGenerator rng;

  public BoundedDistributionDeserializer(RandomGenerator rng) {
    this.rng = rng;
  }

  @Override
  public BoundedDistribution deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    ObjectNode rootNode = jsonParser.getCodec().readTree(jsonParser);

    ObjectNode distributionNode = rootNode.get("distribution").deepCopy();
    distributionNode.putPOJO("rng", new Object());

    ObjectMapper objectMapper = new DataPipelineMapper(rng);
    ImmutableDistribution distribution =
        objectMapper.treeToValue(distributionNode, ImmutableDistribution.class);
    var max = rootNode.get("max").asDouble();
    return ImmutableBoundedDistribution.builder().distribution(distribution).max(max).build();
  }
}
