package uk.co.ramp.io.readers;

import com.google.gson.JsonParser;
import org.junit.Test;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.utilities.ImmutableMinMax;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class PopulationPropertiesReaderTest {
    private static final String mockPopulationProperties = "{ " +
            "  'populationDistribution': { " +
            "    '0': 0.1759, " +
            "    '1': 0.1171, " +
            "    '2': 0.4029, " +
            "    '3': 0.1222, " +
            "    '4': 0.1819 " +
            "  }, " +
            "  populationAges: { " +
            "    '0': { " +
            "      'min': 0, " +
            "      'max': 14 " +
            "    }, " +
            "    '1': { " +
            "      'min': 15, " +
            "      'max': 24 " +
            "    }, " +
            "    '2': { " +
            "      'min': 25, " +
            "      'max': 54 " +
            "    }, " +
            "    '3': { " +
            "      'min': 55, " +
            "      'max': 64 " +
            "    }, " +
            "    '4': { " +
            "      'min': 65, " +
            "      'max': 90 " +
            "    } " +
            "  }, " +
            "  'genderBalance': 0.99 " +
            "}";

    @Test
    public void testRead() {
        var underlyingReader = new BufferedReader(new StringReader(mockPopulationProperties));
        var reader = new PopulationPropertiesReader();
        PopulationProperties populationProperties = reader.read(underlyingReader);

        assertThat(populationProperties.genderBalance()).isCloseTo(0.99, offset(1e-6));

        assertThat(populationProperties.populationDistribution()).containsOnlyKeys(0, 1, 2, 3, 4);
        assertThat(populationProperties.populationDistribution().get(0)).isCloseTo(0.1759, offset(1e-6));
        assertThat(populationProperties.populationDistribution().get(1)).isCloseTo(0.1171, offset(1e-6));
        assertThat(populationProperties.populationDistribution().get(2)).isCloseTo(0.4029, offset(1e-6));
        assertThat(populationProperties.populationDistribution().get(3)).isCloseTo(0.1222, offset(1e-6));
        assertThat(populationProperties.populationDistribution().get(4)).isCloseTo(0.1819, offset(1e-6));

        assertThat(populationProperties.populationAges()).containsOnlyKeys(0, 1, 2, 3, 4);
        assertThat(populationProperties.populationAges().get(0)).isEqualTo(ImmutableMinMax.of(0, 14));
        assertThat(populationProperties.populationAges().get(1)).isEqualTo(ImmutableMinMax.of(15, 24));
        assertThat(populationProperties.populationAges().get(2)).isEqualTo(ImmutableMinMax.of(25, 54));
        assertThat(populationProperties.populationAges().get(3)).isEqualTo(ImmutableMinMax.of(55, 64));
        assertThat(populationProperties.populationAges().get(4)).isEqualTo(ImmutableMinMax.of(65, 90));
    }

    @Test
    public void testCreate() throws IOException {
        var stringWriter = new StringWriter();
        try (BufferedWriter bw = new BufferedWriter(stringWriter)) {
            new PopulationPropertiesReader().create(bw);
        }

        var expectedPopulationSettingsJsonElement = JsonParser.parseString(mockPopulationProperties);

        var actualPopulationSettingsString = stringWriter.toString();
        var actualPopulationSettingsJsonElement = JsonParser.parseString(actualPopulationSettingsString);

        assertThat(actualPopulationSettingsJsonElement).isEqualTo(expectedPopulationSettingsJsonElement);
    }
}