package uk.co.ramp.statistics;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.ramp.TestUtils;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.statistics.types.ImmutableInfection;
import uk.co.ramp.statistics.types.Infection;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class StatisticsWriterTest {

    StatisticsWriter statisticsWriter;
    StatisticsRecorder statisticsRecorder;
    StandardProperties properties;
    Random random = TestUtils.getRandom();

    @Before
    public void setup() {
        statisticsRecorder = Mockito.mock(StatisticsRecorder.class);
        properties = Mockito.mock(StandardProperties.class);
        when(properties.timeStepsPerDay()).thenReturn(1);
        statisticsWriter = new StatisticsWriter(statisticsRecorder, properties);


        Map<Integer, List<Infection>> infections = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            int exposedTime = random.nextInt(3);
            int furtherInfections = random.nextInt(10);
            Case seedCase = Mockito.mock(Case.class);
            when(seedCase.id()).thenReturn(i);
            when(seedCase.exposedTime()).thenReturn(exposedTime);
            Infection infection = ImmutableInfection.builder().seed(i).infections(furtherInfections).build();
            infections.compute(exposedTime, (k, v) -> v == null ? List.of(infection) : Stream.of(List.of(infection), v).flatMap(Collection::stream).collect(Collectors.toList()));
        }

        when(statisticsRecorder.getR0Progression()).thenReturn(infections);

        Map<Integer, Integer> map1 = createMap(10);
        when(statisticsRecorder.getPersonDaysIsolation()).thenReturn(map1);
        Map<Integer, Integer> map2 = createMap(10);
        when(statisticsRecorder.getPeopleInfected()).thenReturn(map2);
        Map<Integer, Integer> map3 = createMap(10);

        when(statisticsRecorder.getContactsTraced()).thenReturn(map3);

    }

    private Map<Integer, Integer> createMap(int keyMax) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < keyMax; i++) {
            map.put(i, random.nextInt(keyMax));
        }
        return map;
    }

    @Test
    public void outputR() throws IOException {

        Writer writer = new StringWriter();
        statisticsWriter.outputR(10, writer);

        List<String> contents = List.of(writer.toString().split("\n"));

        assertThat(contents.size()).isEqualTo(4);
        assertThat(contents.get(0))
                .isEqualTo("\"time\",\"newInfectors\",\"newInfections\",\"r\",\"sevenDayAverageR\"");
    }

    @Test
    public void outputGeneralStats() throws IOException {

        Writer writer = new StringWriter();
        statisticsWriter.outputGeneralStats(writer);

        assertThat(writer.toString()).contains("Person Days in Isolation ,");
        assertThat(writer.toString()).contains("People infected ,");
        assertThat(writer.toString()).contains("Contacts Traced ,");

    }
}