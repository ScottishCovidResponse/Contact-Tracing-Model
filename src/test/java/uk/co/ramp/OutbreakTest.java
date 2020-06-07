package uk.co.ramp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.EventList;
import uk.co.ramp.event.EventProcessor;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;
import uk.co.ramp.io.InitialCaseReader;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.utilities.UtilitiesBean;

import java.io.FileNotFoundException;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.*;

@SuppressWarnings("unchecked")
@DirtiesContext
@RunWith(SpringRunner.class)
@Import({TestConfig.class, TestUtils.class, AppConfig.class})
public class OutbreakTest {

    private final Random random = TestUtils.getRandom();

    @Rule
    public LogSpy logSpy = new LogSpy();

    @Mock
    @Autowired
    EventProcessor eventProcessor;

    @Autowired
    UtilitiesBean utilitiesBean;
    @Autowired
    DistributionSampler distributionSampler;

    @Autowired
    DiseaseProperties diseaseProperties;

    @Autowired
    StandardProperties standardProperties;
    @Autowired
    private Outbreak outbreak;

    @Before

    public void setUp() {

        outbreak.setPopulation(mock(Map.class));

    }

    @Test
    @DirtiesContext
    public void runContactDataWithRandoms() throws FileNotFoundException {
        DiseaseProperties d = TestUtils.diseaseProperties();

        int days = 11;
        int popSize = 500;
        double randomInfection = 0.1;

        Map<Integer, Case> population = new HashMap<>();

        for (int i = 0; i < popSize; i++) {
            Human human = mock(Human.class);
            when(human.id()).thenReturn(i);
            Case thisCase = new Case(human);
            population.put(i, thisCase);
        }

        EventList contacts = createContactRecords(days, population);

        ReflectionTestUtils.setField(outbreak, "eventList", contacts);

        outbreak.setPopulation(population);
//        outbreak.setEventList(contacts);
//        outbreak.setDiseaseProperties(d);
//
//        outbreak.setEventProcessor(eventProcessor);
        outbreak.runContactData(days - 1, randomInfection);

        long sus = population.values().stream().map(Case::virusStatus).filter(status -> status == SUSCEPTIBLE).count();

        // we expect this to roughly follow an exp decay
        double test = popSize * Math.exp(-randomInfection * days);
        Assert.assertEquals(test / (double) popSize, sus / (double) popSize, 0.1);
    }


//    @Test
//    public void getMostSevere() {
//        // case 1, person 2 worse
//        Case person1 = mock(Case.class);
//        Case person2 = mock(Case.class);
//
//        when(person1.status()).thenReturn(SUSCEPTIBLE);
//        when(person2.status()).thenReturn(EXPOSED);
//
//        Case mostSevere = utils.getMostSevere(person1, person2);
//        Assert.assertEquals(person2, mostSevere);
//
//        // case 2, equal, defaults to person 2
//        when(person2.status()).thenReturn(SUSCEPTIBLE);
//        mostSevere = utils.getMostSevere(person1, person2);
//        Assert.assertEquals(person2, mostSevere);
//
//        // case 3, equal, defaults to person 2
//        when(person1.status()).thenReturn(PRESYMPTOMATIC);
//        when(person2.status()).thenReturn(PRESYMPTOMATIC);
//
//        mostSevere = utils.getMostSevere(person1, person2);
//        Assert.assertEquals(person2, mostSevere);
//
//
//        // case 2, person 1 worse, behaves correctly
//        when(person1.status()).thenReturn(PRESYMPTOMATIC);
//        when(person2.status()).thenReturn(EXPOSED);
//
//        mostSevere = utils.getMostSevere(person1, person2);
//        Assert.assertEquals(person1, mostSevere);
//
//
//    }


    @Test
    @DirtiesContext
    public void testPropagate() throws FileNotFoundException {
        int popSize = 100;

        DiseaseProperties d = TestUtils.diseaseProperties();
//        outbreak.setDiseaseProperties(d);
//        outbreak.setEventProcessor(eventProcessor);

        StandardProperties standardProperties = mock(StandardProperties.class);
        when(standardProperties.timeLimit()).thenReturn(100);
        when(standardProperties.initialExposures()).thenReturn(popSize / 10);
        when(standardProperties.populationSize()).thenReturn(popSize);

        Set<Integer> cases = generateTestCases(popSize / 10, popSize);

        InitialCaseReader initialCaseReader = mock(InitialCaseReader.class);
        when(initialCaseReader.getCases()).thenReturn(cases);

        Map<Integer, Case> population = new HashMap<>();
        for (int i = 0; i < popSize; i++) {
            Human human = mock(Human.class);
            when(human.id()).thenReturn(i);
            Case thisCase = new Case(human);
            population.put(i, thisCase);
        }

        EventList contacts = createContactRecords(200, population);

        ReflectionTestUtils.setField(outbreak, "eventList", contacts);
        ReflectionTestUtils.setField(outbreak, "properties", standardProperties);
        outbreak.setPopulation(population);


        long susceptible = population.values().stream().map(Case::virusStatus).filter(status -> status == SUSCEPTIBLE).count();

        Map<Integer, CmptRecord> records = outbreak.propagate();


        long susceptiblePost = getPopulationViaReflection().values().stream().map(Case::virusStatus).filter(status -> status == SUSCEPTIBLE).count();

        Assert.assertEquals(records.size(), standardProperties.timeLimit() + 1);
        Assert.assertTrue(susceptiblePost < susceptible);
        Assert.assertThat(logSpy.getOutput(), containsString("Generated initial outbreak of " + popSize / 10 + " cases"));
        Assert.assertThat(logSpy.getOutput(), containsString("Not all contact data will be used"));

    }


    @Test
    @DirtiesContext
    public void runToCompletionAllContact() throws FileNotFoundException {
        int popSize = 100;
        int infections = 1 + popSize / 10;

        DiseaseProperties d = TestUtils.diseaseProperties();
//        outbreak.setDiseaseProperties(d);
//        outbreak.setEventProcessor(eventProcessor);
        Map<Integer, Case> population = new HashMap<>();

        for (int i = 0; i < popSize; i++) {
            Human h = mock(Human.class);
            when(h.id()).thenReturn(i);
            Case thisCase = new Case(h);
            population.put(i, thisCase);
        }
        Set<Integer> cases = generateTestCases(infections, popSize);
        InitialCaseReader initialCaseReader = mock(InitialCaseReader.class);
        when(initialCaseReader.getCases()).thenReturn(cases);

        StandardProperties properties = mock(StandardProperties.class);
        when(properties.initialExposures()).thenReturn(infections);
        when(properties.timeLimit()).thenReturn(100);
        when(properties.populationSize()).thenReturn(popSize);
        when(properties.steadyState()).thenReturn(false);


        EventList contacts = createContactRecords(500, population);

        ReflectionTestUtils.setField(outbreak, "eventList", contacts);
        outbreak.setPopulation(population);
//        outbreak.setEventList(contacts);
//        outbreak.setStandardProperties(properties);
//        outbreak.setInitialCaseReader(initialCaseReader);
//        outbreak.setDiseaseProperties(d);
        outbreak.generateInitialInfection();

        long susceptible = population.values().stream().map(Case::virusStatus).filter(status -> status == SUSCEPTIBLE).count();
        outbreak.runToCompletion();

        long susceptiblePost = getPopulationViaReflection().values().stream().map(Case::virusStatus).filter(status -> status == SUSCEPTIBLE).count();

        Assert.assertTrue(susceptiblePost < susceptible);
        Assert.assertThat(logSpy.getOutput(), containsString("Not all contact data will be used"));

    }


    @Test
    @DirtiesContext
    public void runToCompletionSmallContact() {
        int popSize = 100;
        int infections = 1 + popSize / 10;

        Map<Integer, Case> population = new HashMap<>();

        for (int i = 0; i < popSize; i++) {
            Human h = mock(Human.class);
            when(h.id()).thenReturn(i);
            Case thisCase = new Case(h);
            population.put(i, thisCase);
        }
        Set<Integer> cases = generateTestCases(infections, popSize);
        InitialCaseReader initialCaseReader = mock(InitialCaseReader.class);
        when(initialCaseReader.getCases()).thenReturn(cases);

        EventList contacts = createContactRecords(5, population);

        ReflectionTestUtils.setField(outbreak, "eventList", contacts);

        outbreak.setPopulation(population);
        outbreak.generateInitialInfection();

        long susceptible = population.values().stream().map(Case::virusStatus).filter(status -> status == SUSCEPTIBLE).count();
        outbreak.runToCompletion();

        long susceptiblePost = getPopulationViaReflection().values().stream().map(Case::virusStatus).filter(status -> status == SUSCEPTIBLE).count();

        Assert.assertTrue(susceptiblePost < susceptible);
        Assert.assertThat(logSpy.getOutput(), containsString("There are no active cases and the random infection rate is zero."));
        Assert.assertThat(logSpy.getOutput(), containsString("Exiting as solution is stable."));

    }


    @Test
    public void runContactDataWithoutRandoms() {

        int days = 11;
        int popSize = 500;
        double randomInfection = 0.0;

        Map<Integer, Case> population = new HashMap<>();

        for (int i = 0; i < popSize; i++) {
            Human human = mock(Human.class);
            Case thisCase = new Case(human);
            population.put(i, thisCase);
        }

        EventList contacts = createContactRecords(days, population);

        ReflectionTestUtils.setField(outbreak, "eventList", contacts);
        outbreak.setPopulation(population);
        outbreak.runContactData(days - 1, randomInfection);

        Assert.assertThat(logSpy.getOutput(), containsString("There are no active cases and the random infection rate is zero."));
        Assert.assertThat(logSpy.getOutput(), containsString("Exiting as solution is stable."));

    }

    private EventList createContactRecords(int days, Map<Integer, Case> population) {
        Map<Integer, List<ContactEvent>> contacts = new HashMap<>();
        for (int i = 0; i < days; i++) {

            List<ContactEvent> dailyContacts = new ArrayList<>();
            for (int j = 0; j < random.nextInt(population.size()); j++) {

                int personA = random.nextInt(population.size());
                int personB = random.nextInt(population.size());
                int weight = random.nextInt(100);

                dailyContacts.add(ImmutableContactEvent.builder().from(personA).to(personB).label("").weight(weight).time(i).build());

            }
            contacts.put(i, dailyContacts);
        }
        EventList eventList = new EventList();
        eventList.addEvents(contacts);

        return eventList;
    }

    @Test
    public void updateLogActiveCases() {
        int time = 0;
        int numStatus = values().length;
        int inactiveCases = 0;
        Map<Integer, Case> population = new HashMap<>();
        for (int i = 0; i < 500; i++) {
            VirusStatus status = VirusStatus.values()[random.nextInt(numStatus)];
            if (status == SUSCEPTIBLE || status == RECOVERED || status == DEAD) inactiveCases++;
            Case thisCase = Mockito.mock(Case.class);
            when(thisCase.virusStatus()).thenReturn(status);
            when(thisCase.id()).thenReturn(i);

            population.put(i, thisCase);
        }

        outbreak.setPopulation(population);

        ReflectionTestUtils.setField(outbreak, "activeCases", 123);
        outbreak.updateLogActiveCases(time);
        int activeCases = (int) ReflectionTestUtils.getField(outbreak, "activeCases");

        Assert.assertEquals(population.size(), activeCases + inactiveCases);

    }

    private Map<Integer, Case> getPopulationViaReflection() {
        Map<Integer, Case> population = (Map<Integer, Case>) ReflectionTestUtils.getField(outbreak, "population");
        Assert.assertNotNull(population);
        Assert.assertTrue(population.size() > 0);

        return population;
    }

    private Set<Integer> generateTestCases(int numCases, int popSize) {
        Set<Integer> cases = new HashSet<>();
        while (cases.size() < numCases) {
            cases.add(random.nextInt(popSize - 1));
        }
        return cases;
    }


}