package uk.co.ramp.io.readers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.*;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.io.types.StandardProperties;

public class StandardPropertiesReaderTest {
  private static final String mockStandardProperties =
      "{"
          + "  'populationSize': 10000,"
          + "  'timeLimitDays': 100,"
          + "  'initialExposures': 1000,"
          + "  'seed': 0,"
          + "  'steadyState': true,"
          + "  'timeStepsPerDay': 4,"
          + "  'timeStepSpread': [0.25,0.25,0.25,0.25]"
          + "}";

  private StandardPropertiesReader standardPropertiesReader;

  @Before
  public void setUp() {
    standardPropertiesReader = new StandardPropertiesReader();
  }

  @Test
  public void testRead() {
    var underlyingReader = new BufferedReader(new StringReader(mockStandardProperties));
    StandardProperties standardProperties = standardPropertiesReader.read(underlyingReader);

    assertThat(standardProperties.initialExposures()).isEqualTo(1000);
    assertThat(standardProperties.populationSize()).isEqualTo(10000);
    assertThat(standardProperties.seed()).hasValue(0);
    assertThat(standardProperties.timeLimitDays()).isEqualTo(100);
    assertThat(standardProperties.steadyState()).isTrue();
    assertThat(standardProperties.timeStepsPerDay()).isEqualTo(4);
    assertThat(standardProperties.timeStepSpread()).containsExactly(0.25, 0.25, 0.25, 0.25);
  }
}
