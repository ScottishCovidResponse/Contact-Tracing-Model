package uk.co.ramp.policy.isolation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.io.StringWriter;
import java.util.AbstractMap;
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
                    .empiricalSamples(List.of(Math.PI))
                    .rng(rng)
                    .build())
            .max(Double.MAX_VALUE)
            .build();

    ObjectMapper mapper = new ObjectMapper();
    SimpleModule simpleModule = new SimpleModule("SimpleModule");
    simpleModule.addSerializer(MinMax.class, new MinMaxSerializer());
    mapper.registerModule(simpleModule);

    bds.serialize(dist, generator, mapper.getSerializerProvider());
    assertThat(writer.toString())
        .isEqualToNormalizingPunctuationAndWhitespace("\"distributionValue\" : 3");
  }

  @Test
  public void write() throws IOException {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = setup(writer);

    String expected = "{ \"runSettings\": \"runSettings.json\" }";
    Map.Entry<String, JsonNode> entry =
        new AbstractMap.SimpleImmutableEntry<String, JsonNode>(
            "runSettings", new TextNode("runSettings.json"));

    generator.writeStartObject();
    bds.write(generator, entry);
    generator.writeEndObject();

    assertThat(writer.toString()).isEqualToNormalizingPunctuationAndWhitespace(expected);
  }

  private JsonGenerator setup(StringWriter writer) throws IOException {
    JsonFactory factory = new JsonFactory();
    JsonGenerator generator = factory.createGenerator(writer);
    generator.setCodec(new ObjectMapper());
    generator.useDefaultPrettyPrinter();
    return generator;
  }
}
