package uk.co.ramp.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.io.readers.AgeDependentHealthReader;
import uk.co.ramp.io.types.AgeDependentHealthList;
import uk.co.ramp.io.types.ImmutableAgeDependentHealth;
import uk.co.ramp.io.types.ImmutableAgeDependentHealthList;
import uk.co.ramp.utilities.ImmutableMinMax;

public class AgeDependentHealthListTest {

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

  AgeDependentHealthList dummyList =
      ImmutableAgeDependentHealthList.builder()
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
  private AgeDependentHealthReader ageDependentHealthReader;

  @Before
  public void setup() {
    ageDependentHealthReader = new AgeDependentHealthReader();
  }

  @Test
  public void testRead() {
    StringReader reader = new StringReader(input);
    var output = ageDependentHealthReader.read(reader);
    assertThat(output).isEqualToComparingFieldByField(dummyList);
  }
}
