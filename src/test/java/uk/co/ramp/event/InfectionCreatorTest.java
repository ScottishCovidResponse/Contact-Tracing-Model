package uk.co.ramp.event;

import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.Population;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.InfectionEvent;
import uk.co.ramp.io.InitialCaseReader;
import uk.co.ramp.people.Case;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.SUSCEPTIBLE;

public class InfectionCreatorTest {
    private DistributionSampler distributionSampler;
    private InitialCaseReader initialCaseReader;

    @Before
    public void setUp() {
        distributionSampler = mock(DistributionSampler.class);
        var distributionValues = List.of(0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95);
        var counter = new AtomicInteger(0);
        when(distributionSampler.uniformBetweenZeroAndOne()).thenAnswer(i -> distributionValues.get(counter.incrementAndGet() % distributionValues.size()));
        initialCaseReader = mock(InitialCaseReader.class);
    }
    @Test
    public void createRandomInfections() {

        Map<Integer, Case> population = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            Case mock0 = mock(Case.class);
            when(mock0.virusStatus()).thenReturn(SUSCEPTIBLE);
            when(mock0.id()).thenReturn(i);
            population.put(i, mock0);
        }
        InfectionCreator infectionCreator = new InfectionCreator(new Population(population), distributionSampler, initialCaseReader);

        List<InfectionEvent> list = infectionCreator.createRandomInfections(0, 0.1, 100);

        assertThat(list.size()).isEqualTo(10);
    }

}