package uk.co.ramp.io.readers;

import com.google.gson.JsonParser;
import org.junit.Test;
import uk.co.ramp.io.InfectionRates;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ramp.people.VirusStatus.*;

public class InfectionRateReaderTest {

    private final String mockRates = "{\n" +
            "  \"SEVERELY_SYMPTOMATIC\": 1.0,\n" +
            "  \"SYMPTOMATIC\":0.9,\n" +
            "  \"ASYMPTOMATIC\": 0.5,\n" +
            "  \"PRESYMPTOMATIC\":0.4\n" +
            "}";
    private final String object = "{\"infectionRates\":{\"PRESYMPTOMATIC\":0.4,\"SEVERELY_SYMPTOMATIC\":1.0,\"SYMPTOMATIC\":0.9,\"ASYMPTOMATIC\":0.5}}";

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

    @Test
    public void testCreate() throws IOException {
        var stringWriter = new StringWriter();
        try (BufferedWriter bw = new BufferedWriter(stringWriter)) {
            new InfectionRateReader().create(bw);
        }

        var expectedInputFilesJsonElement = JsonParser.parseString(object);
        var actualRatesString = stringWriter.toString();
        var actualRatesJsonElement = JsonParser.parseString(actualRatesString);

        assertThat(actualRatesJsonElement).isEqualTo(expectedInputFilesJsonElement);
    }
}