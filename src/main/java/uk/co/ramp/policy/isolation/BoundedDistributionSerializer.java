package uk.co.ramp.policy.isolation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.ConfigurationException;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.ramp.mapper.DataPipelineMapper;

/**
 * This class provides a temporary serialization wrapper over the distribution from std api. This
 * can/will be removed once we migrate ALL 'distribution' related objects write to the data
 * pipeline.
 */
public class BoundedDistributionSerializer extends JsonSerializer<BoundedDistribution> {
  private static final Logger LOGGER = LogManager.getLogger(BoundedDistributionSerializer.class);
  private final RandomGenerator rng;

  public BoundedDistributionSerializer(RandomGenerator rng) {
    this.rng = rng;
  }

  @Override
  public void serialize(
      BoundedDistribution value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    ObjectMapper objectMapper = new DataPipelineMapper(rng);
    JsonNode boundedDistributionJsonNode = objectMapper.valueToTree(value);
    ObjectNode componentsObjectNode = boundedDistributionJsonNode.deepCopy();

    gen.writeStartObject();
    componentsObjectNode
        .fields()
        .forEachRemaining(
            stringJsonNodeEntry -> {
              if (stringJsonNodeEntry.getKey().equals("distribution")) {
                ObjectNode innerDist = stringJsonNodeEntry.getValue().deepCopy();
                innerDist.remove("rng");
                JsonNode innerTree = objectMapper.valueToTree(innerDist);
                write(gen, new SimpleImmutableEntry<>("distribution", innerTree));
                return;
              }

              write(gen, stringJsonNodeEntry);
            });
    gen.writeEndObject();
  }

  public void write(JsonGenerator gen, Entry<String, JsonNode> entry) {
    try {
      gen.writeObjectField(entry.getKey(), entry.getValue());
    } catch (IOException e) {
      String message = "An error occurred writing a JSON object in BoundedDistributionSerializer.";
      LOGGER.error(message);
      throw new ConfigurationException(message, e);
    }
  }
}
