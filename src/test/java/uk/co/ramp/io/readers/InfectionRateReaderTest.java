package uk.co.ramp.io.readers;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ramp.people.VirusStatus.*;

import java.io.*;
import org.junit.Test;
import uk.co.ramp.io.InfectionRates;

public class InfectionRateReaderTest {

  private final String mockRates =
      "{\n"
          + "  \"SEVERELY_SYMPTOMATIC\": 1.0,\n"
          + "  \"SYMPTOMATIC\":0.9,\n"
          + "  \"ASYMPTOMATIC\": 0.5,\n"
          + "  \"PRESYMPTOMATIC\":0.4\n"
          + "}";
  private final String object =
      "{\"infectionRates\":{\"PRESYMPTOMATIC\":0.4,\"SEVERELY_SYMPTOMATIC\":1.0,\"SYMPTOMATIC\":0.9,\"ASYMPTOMATIC\":0.5}}";

  private final Reader reader = new StringReader(mockRates);

  @Test
  public void read() {
    InfectionRateReader infectionRateReader = new InfectionRateReader();
    InfectionRates infectionRates = infectionRateReader.read(reader);

    assertThat(infectionRates.getInfectionRates().size()).isEqualTo(4);
    assertThat(infectionRates.getInfectionRate(SEVERELY_SYMPTOMATIC)).isEqualTo(1.0);
    assertThat(infectionRates.getInfectionRate(SYMPTOMATIC)).isEqualTo(0.9);
    assertThat(infectionRates.getInfectionRate(ASYMPTOMATIC)).isEqualTo(0.5);
    assertThat(infectionRates.getInfectionRate(PRESYMPTOMATIC)).isEqualTo(0.4);
    assertThat(infectionRates.getInfectionRate(DEAD)).isEqualTo(0d);
  }
}
