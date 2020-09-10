package uk.co.ramp.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.io.readers.PopulationOverridesReader;
import uk.co.ramp.io.types.ImmutableAgeDependentHealth;
import uk.co.ramp.io.types.ImmutablePopulationOverrides;
import uk.co.ramp.io.types.PopulationOverrides;
import uk.co.ramp.utilities.ImmutableMinMax;

public class PopulationSettingsTest {

  private final PopulationOverrides dummyList =
      ImmutablePopulationOverrides.builder()
          .addAgeDependentList(
              ImmutableAgeDependentHealth.builder()
                  .range(ImmutableMinMax.of(0, 19))
                  .modifier(1.d)
                  .build())
          .addAgeDependentList(
              ImmutableAgeDependentHealth.builder()
                  .range(ImmutableMinMax.of(20, 39))
                  .modifier(0.9)
                  .build())
          .addAgeDependentList(
              ImmutableAgeDependentHealth.builder()
                  .range(ImmutableMinMax.of(40, 59))
                  .modifier(0.8)
                  .build())
          .addAgeDependentList(
              ImmutableAgeDependentHealth.builder()
                  .range(ImmutableMinMax.of(60, 79))
                  .modifier(0.6)
                  .build())
          .addAgeDependentList(
              ImmutableAgeDependentHealth.builder()
                  .range(ImmutableMinMax.of(80, 100))
                  .modifier(0.4)
                  .build())
          .build();
  private PopulationOverridesReader populationOverridesReader;

  @Before
  public void setup() {
    populationOverridesReader = new PopulationOverridesReader();
  }

  @Test
  public void testRead() {
    String input =
        "{\n"
            + "  \"ageDependentList\": [\n"
            + "    {\n"
            + "      \"range\": {\n"
            + "        \"min\": 0,\n"
            + "        \"max\": 19\n"
            + "      },\n"
            + "      \"modifier\": 1.0\n"
            + "    },\n"
            + "    {\n"
            + "      \"range\": {\n"
            + "        \"min\": 20,\n"
            + "        \"max\": 39\n"
            + "      },\n"
            + "      \"modifier\": 0.9\n"
            + "    },\n"
            + "    {\n"
            + "      \"range\": {\n"
            + "        \"min\": 40,\n"
            + "        \"max\": 59\n"
            + "      },\n"
            + "      \"modifier\": 0.8\n"
            + "    },\n"
            + "    {\n"
            + "      \"range\": {\n"
            + "        \"min\": 60,\n"
            + "        \"max\": 79\n"
            + "      },\n"
            + "      \"modifier\": 0.6\n"
            + "    },\n"
            + "    {\n"
            + "      \"range\": {\n"
            + "        \"min\": 80,\n"
            + "        \"max\": 100\n"
            + "      },\n"
            + "      \"modifier\": 0.4\n"
            + "    }\n"
            + "  ]\n"
            + "}";
    StringReader reader = new StringReader(input);
    var output = populationOverridesReader.read(reader);
    assertThat(output).isEqualToComparingFieldByField(dummyList);
  }
}
