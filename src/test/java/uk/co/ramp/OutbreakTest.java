package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.contact.ImmutableContactRecord;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ProgressionDistribution;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.record.CmptRecord;
import uk.co.ramp.utilities.UtilitiesBean;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.co.ramp.people.VirusStatus.*;

public class OutbreakTest {

    private final Random random = TestUtils.getRandom();
    private final UtilitiesBean utils = new UtilitiesBean();
    @Rule
    public LogSpy logSpy = new LogSpy();
    private Outbreak outbreak;

    @Before
    public void setUp() throws Exception {
        outbreak = new Outbreak();
        outbreak.setPopulation(mock(Map.class));
        outbreak.setStandardProperties(mock(StandardProperties.class));
        outbreak.setDiseaseProperties(mock(DiseaseProperties.class));
        outbreak.setRandomDataGenerator(mock(RandomDataGenerator.class));
        outbreak.setUtilitiesBean(utils);
    }


    @Test
    public void chooseInitialInfected() {

        int popSize = random.nextInt(1000) + 500;
        int infected = random.nextInt(popSize / 2);

        StandardProperties props = mock(StandardProperties.class);
        when(props.populationSize()).thenReturn(popSize);
        when(props.infected()).thenReturn(infected);
        outbreak.setStandardProperties(props);
        outbreak.setRandomDataGenerator(new RandomDataGenerator(RandomGeneratorFactory.createRandomGenerator(random)));

        Set<Integer> set = outbreak.chooseInitialInfected();

        Assert.assertEquals(set.size(), infected);

        int max = set.stream().mapToInt(Integer::intValue).max().orElseThrow();
        int min = set.stream().mapToInt(Integer::intValue).min().orElseThrow();

        Assert.assertTrue(max < popSize);
        Assert.assertTrue(min >= 0);


    }

    @Test
    public void getMostSevere() {
        // case 1, person 2 worse
        Case person1 = mock(Case.class);
        Case person2 = mock(Case.class);

        when(person1.status()).thenReturn(SUSCEPTIBLE);
        when(person2.status()).thenReturn(EXPOSED);

        Case mostSevere = utils.getMostSevere(person1, person2);
        Assert.assertEquals(person2, mostSevere);

        // case 2, equal, defaults to person 2
        when(person2.status()).thenReturn(SUSCEPTIBLE);
        mostSevere = utils.getMostSevere(person1, person2);
        Assert.assertEquals(person2, mostSevere);

        // case 3, equal, defaults to person 2
        when(person1.status()).thenReturn(INFECTED_SYMP);
        when(person2.status()).thenReturn(INFECTED_SYMP);

        mostSevere = utils.getMostSevere(person1, person2);
        Assert.assertEquals(person2, mostSevere);


        // case 2, person 1 worse, behaves correctly
        when(person1.status()).thenReturn(INFECTED_SYMP);
        when(person2.status()).thenReturn(EXPOSED_2);

        mostSevere = utils.getMostSevere(person1, person2);
        Assert.assertEquals(person1, mostSevere);


    }


    @Test
    public void evaluateExposures() {
        int personA = random.nextInt(100);
        Case caseA = mock(Case.class);
        int personB = random.nextInt(100);
        Case caseB = mock(Case.class);
        int time = random.nextInt(100);


        when(caseA.status()).thenReturn(SUSCEPTIBLE);
        when(caseB.status()).thenReturn(INFECTED_SYMP);
        doCallRealMethod().when(caseA).setExposedBy(anyInt());
        when(caseA.isInfectious()).thenReturn(false);
        when(caseB.isInfectious()).thenReturn(true);
        when(caseB.id()).thenReturn(personB);
        when(caseA.id()).thenReturn(personA);

        DiseaseProperties diseaseProperties = mock(DiseaseProperties.class);
        when(diseaseProperties.progressionDistribution()).thenReturn(ProgressionDistribution.FLAT);
        outbreak.setDiseaseProperties(diseaseProperties);

        RandomDataGenerator rng = mock(RandomDataGenerator.class);
        when(rng.nextUniform(0, 1)).thenReturn(0.d);

        Map<Integer, Case> population = mock(Map.class);
        when(population.get(personA)).thenReturn(caseA);
        when(population.get(personB)).thenReturn(caseB);
        ContactRecord contact = ImmutableContactRecord.builder().from(personA).to(personB).time(time).weight(50).build();

        outbreak.setPopulation(population);

        outbreak.evaluateExposures(population, contact, time);

        verify(caseA, times(1)).setStatus(any());

        ArgumentCaptor<VirusStatus> captor = ArgumentCaptor.forClass(VirusStatus.class);
        verify(caseA).setStatus(captor.capture());
        VirusStatus interceptedStatus = captor.getValue();

        ArgumentCaptor<Integer> captor2 = ArgumentCaptor.forClass(Integer.class);
        verify(caseA).setExposedBy(captor2.capture());
        int interceptedId = captor2.getValue();

        Assert.assertEquals(EXPOSED, interceptedStatus);
        Assert.assertEquals(personB, interceptedId);

    }

    @Test
    @Ignore
    public void propagate() {
        Map<Integer, List<ContactRecord>> contactRecords = new HashMap<>();

        Map<Integer, Case> population = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            population.put(i, Mockito.mock(Case.class));
        }


        outbreak.setPopulation(population);

        StandardProperties standardProperties = Mockito.mock(StandardProperties.class);
        when(standardProperties.infected()).thenReturn(10);

        for (int t = 0; t < 5; t++) {
            List<ContactRecord> cr = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                cr.add(ImmutableContactRecord.builder().from(random.nextInt(5)).to(random.nextInt(5)).time(t).weight(50).build());
            }

            contactRecords.put(t, cr);
        }


        outbreak.setContactRecords(contactRecords);

        Map<Integer, CmptRecord> var = outbreak.propagate();


    }


    @Test
    public void setPopulation() {
    }

    @Test
    public void setContactRecords() {
    }

    @Test
    public void setDiseaseProperties() {
    }

    @Test
    public void setStandardProperties() {
    }

    @Test
    public void setRandomDataGenerator() {
    }

    @Test
    public void setUtilitiesBean() {
    }

    @Test
    public void testPropagate() {
    }

    @Test
    public void generateInitialInfection() {
    }

    @Test
    public void runToCompletion() {
    }

    @Test
    public void runContactData() {
    }

    @Test
    public void evaluateContact() {
    }

    @Test
    public void runToSteadyState() {
    }

    @Test
    public void updatePopulationState() {
    }

    @Test
    public void alertPopulation() {
    }

    @Test
    public void testEvaluateExposures() {
    }

    @Test
    public void testChooseInitialInfected() {
    }

    @Test
    public void calculateDailyStatistics() {
    }
}