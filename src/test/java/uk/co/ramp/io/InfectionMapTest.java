package uk.co.ramp.io;

import org.apache.logging.log4j.core.util.StringBuilderWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.co.ramp.LogSpy;
import uk.co.ramp.TestUtils;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;
import static uk.co.ramp.people.VirusStatus.RECOVERED;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

public class InfectionMapTest {

    private final Random random = TestUtils.getRandom();
    private final Map<VirusStatus, Integer> counts = new EnumMap<>(VirusStatus.class);
    private final int populationSize = 100;
    @Rule
    public LogSpy logSpy = new LogSpy();
    private InfectionMap infectionMap;
    private Set<Integer> seeds;

    @Before
    public void setup() {
        Map<Integer, Case> population = new HashMap<>();
        seeds = new HashSet<>();

        var statuses = VirusStatus.values();
        var count = statuses.length;

        for (int i = 0; i < 10; i++) {
            seeds.add(random.nextInt(populationSize));
        }

        for (int i = 0; i < populationSize; i++) {

            if (seeds.contains(i)) {
                Case thisCase = mock(Case.class);
                when(thisCase.id()).thenReturn(i);
                when(thisCase.virusStatus()).thenReturn(RECOVERED);
                when(thisCase.exposedBy()).thenReturn(Case.getInitial());
                when(thisCase.getSource()).thenCallRealMethod();
                population.put(i, thisCase);
            } else {
                VirusStatus virusStatus = statuses[random.nextInt(count)];
                int infector = new ArrayList<>(seeds).get(random.nextInt(seeds.size()));

                counts.merge(virusStatus, 1, (prev, one) -> prev + 1);

                Case thisCase = mock(Case.class);
                when(thisCase.id()).thenReturn(i);
                when(thisCase.virusStatus()).thenReturn(virusStatus);
                when(thisCase.exposedBy()).thenReturn(infector);
                when(thisCase.getSource()).thenCallRealMethod();
                population.put(i, thisCase);

            }
        }

        infectionMap = new InfectionMap(population);

    }


    @Test
    public void outputMap() {

        StringWriter stringWriter = new StringWriter();
        infectionMap.outputMap(stringWriter);

        Set<Integer> lines = Arrays.stream(
                stringWriter.toString().split("\n")).
                filter(s -> !s.isBlank()).
                map(s -> s.substring(0, s.indexOf("("))).
                map(Integer::parseInt).
                collect(Collectors.toSet());

        Assert.assertEquals(lines, seeds);
    }

    @Test(expected = InfectionMapException.class)
    public void testException() throws IOException {

        infectionMap = mock(InfectionMap.class);
        doCallRealMethod().when(infectionMap).outputMap(any());
        doThrow(new IOException("")).when(infectionMap).recurseSet(anyList(), anyMap(), any(), anyInt());

        try {
            infectionMap.outputMap(new StringWriter());
        } catch (InfectionMapException e) {
            Assert.assertThat(logSpy.getOutput(), containsString("An error occurred while writing the map file"));
            throw e;
        }
    }

    @Test
    public void collectInfectors() {

        Map<Integer, Set<Case>> temp = infectionMap.collectInfectors();
        Assert.assertEquals(seeds.size(), temp.get(Case.getInitial()).size());

        int sum = seeds.stream().mapToInt(seed -> temp.get(seed).size()).sum();

        Assert.assertEquals(sum, populationSize - counts.get(SUSCEPTIBLE) - seeds.size());

    }


    @Test
    public void recurseSet() throws IOException {

        int tab = 1;
        Map<Integer, Set<Case>> infectors = new HashMap<>();

        Case root = mock(Case.class);
        when(root.id()).thenReturn(0);
        when(root.exposedBy()).thenReturn(Case.getInitial());
        when(root.exposedTime()).thenReturn(10);
        when(root.getSource()).thenCallRealMethod();

        Case first = mock(Case.class);
        when(first.id()).thenReturn(1);
        when(first.exposedBy()).thenReturn(0);
        when(first.exposedTime()).thenReturn(10);
        when(first.getSource()).thenCallRealMethod();
        infectors.putIfAbsent(root.id(), Set.of(first));

        Case second = mock(Case.class);
        when(second.id()).thenReturn(2);
        when(second.exposedBy()).thenReturn(1);
        when(second.exposedTime()).thenReturn(10);
        when(second.getSource()).thenCallRealMethod();
        infectors.putIfAbsent(first.id(), Set.of(second));

        List<Case> target = List.of(root);
        Writer writer = new StringBuilderWriter();
        infectionMap.recurseSet(target, infectors, writer, tab);

        Assert.assertEquals(writer.toString().trim(), "0(0)          ->  [1(0)]\n" +
                "              ->  1(0)           ->  [2(0)]");

    }
}