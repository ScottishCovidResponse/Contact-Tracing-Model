package uk.co.ramp.policy.isolation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.distribution.BoundedDistribution;
import uk.co.ramp.distribution.ImmutableBoundedDistribution;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.ImmutableDistribution;
import uk.ramp.distribution.MinMax;
import uk.ramp.distribution.MinMaxSerializer;

public class BoundedDistributionSerializerTest {

  private BoundedDistributionSerializer bds;
  private RandomGenerator rng;

  @Before
  public void setup() {
    bds = new BoundedDistributionSerializer(rng);
    rng = new JDKRandomGenerator(123);
  }

  @Test
  public void serialize() throws IOException {
    JsonFactory factory = new JsonFactory();
    StringWriter writer = new StringWriter();

    JsonGenerator generator = factory.createGenerator(writer);
    generator.setCodec(new ObjectMapper());
    generator.useDefaultPrettyPrinter();

    BoundedDistribution dist =
        ImmutableBoundedDistribution.builder()
            .distribution(
                ImmutableDistribution.builder()
                    .internalType(Distribution.DistributionType.empirical)
                    .empiricalSamples(List.of(Double.MAX_VALUE))
                    .rng(rng)
                    .build())
            .max(Double.MAX_VALUE)
            .build();

    ObjectMapper mapper = new ObjectMapper();
    SimpleModule simpleModule = new SimpleModule("SimpleModule");
    simpleModule.addSerializer(MinMax.class, new MinMaxSerializer());
    mapper.registerModule(simpleModule);

    bds.serialize(dist, generator, mapper.getSerializerProvider());
    assertThat(writer.toString()).contains("\"distributionValue\" : 2147483647");
  }

  @Test
  public void write() throws IOException {
    JsonFactory factory = new JsonFactory();
    StringWriter writer = new StringWriter();
    String input =
        "{\n"
            + "  \"runSettings\": \"runSettings.json\",\n"
            + "  \"contactData\": \"homogeneous_contacts.csv\",\n"
            + "  \"ageData\": \"ids_Paul.csv\",\n"
            + "  \"initialExposures\": \"initialExposures.csv\",\n"
            + "  \"tracingPolicies\": \"tracingPolicies.json\",\n"
            + "  \"isolationPolicies\": \"isolationPolicies.json\",\n"
            + "  \"infectionRates\": \"infectionRates.json\",\n"
            + "  \"ageDependentHealth\": \"ageDependentHealth.json\"\n"
            + "}";
    StringReader reader = new StringReader(input);

    JsonGenerator generator = factory.createGenerator(writer);
    generator.setCodec(new ObjectMapper());
    generator.useDefaultPrettyPrinter();
    generator.writeStartObject();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode rootNode = objectMapper.readTree(reader);
    Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();

    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> entry = fields.next();
      bds.write(generator, entry);
    }
    generator.writeEndObject();

    assertThat(writer.toString()).isEqualToNormalizingPunctuationAndWhitespace(input);
  }
}
