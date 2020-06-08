package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.contact.ContactRecord;
import uk.co.ramp.contact.ImmutableContactRecord;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.InitialCaseReader;
import uk.co.ramp.io.ProgressionDistribution;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.people.AlertStatus;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.utilities.UtilitiesBean;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.co.ramp.people.AlertStatus.ALERTED;
import static uk.co.ramp.people.VirusStatus.*;

@SuppressWarnings("unchecked")
public class OutbreakTest {

    private final Random random = TestUtils.getRandom();
    private final UtilitiesBean utils = new UtilitiesBean();
    @Rule
    public LogSpy logSpy = new LogSpy();
    private Outbreak outbreak;

    @Before

    public void setUp() {
        outbreak = new Outbreak();
        outbreak.setPopulation(mock(Map.class));
        outbreak.setStandardProperties(mock(StandardProperties.class));
        outbreak.setDiseaseProperties(mock(DiseaseProperties.class));
        outbreak.setRandomDataGenerator(TestUtils.dataGenerator());
        outbreak.setUtilitiesBean(utils);
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

        outbreak.evaluateExposures(contact, time);

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
    public void testPropagate() throws FileNotFoundException {
        int popSize = 100;

        DiseaseProperties d = TestUtils.diseaseProperties();
        outbreak.setDiseaseProperties(d);

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

        Map<Integer, List<ContactRecord>> contacts = createContactRecords(200, population);

        outbreak.setPopulation(population);
        outbreak.setContactRecords(contacts);
        outbreak.setStandardProperties(standardProperties);
        outbreak.setInitialCaseReader(initialCaseReader);


        long susceptible = population.values().stream().map(Case::status).filter(status -> status == SUSCEPTIBLE).count();

        outbreak.propagate();

        long susceptiblePost = getPopulationViaReflection().values().stream().map(Case::status).filter(status -> status == SUSCEPTIBLE).count();


        Assert.assertTrue(susceptiblePost < susceptible);
        Assert.assertThat(logSpy.getOutput(), containsString("Generated initial outbreak of " + popSize / 10 + " cases"));
        Assert.assertThat(logSpy.getOutput(), containsString("Not all contact data will be used"));
        Assert.assertThat(logSpy.getOutput(), containsString("There are no active cases and the random infection rate is zero."));
        Assert.assertThat(logSpy.getOutput(), containsString("Exiting as solution is stable."));


    }

    private Set<Integer> generateTestCases(int numCases, int popSize) {
        Set<Integer> cases = new HashSet<>();
        while (cases.size() < numCases) {
            cases.add(random.nextInt(popSize - 1));
        }
        return cases;
    }

    @Test
    public void generateInitialInfection() {

        int popSize = 4000;
        Map<Integer, Case> population = new HashMap<>();
        int infected = random.nextInt(popSize / 4) + 1;
        for (int i = 0; i < popSize; i++) {
            Human h = mock(Human.class);
            when(h.id()).thenReturn(i);
            Case thisCase = new Case(h);
            population.put(i, thisCase);
        }
        Set<Integer> cases = generateTestCases(infected, popSize);
        InitialCaseReader initialCaseReader = mock(InitialCaseReader.class);
        when(initialCaseReader.getCases()).thenReturn(cases);

        StandardProperties properties = mock(StandardProperties.class);
        when(properties.initialExposures()).thenReturn(infected);
        when(properties.populationSize()).thenReturn(population.size());

        DiseaseProperties diseaseProperties = mock(DiseaseProperties.class);
        when(diseaseProperties.progressionDistribution()).thenReturn(ProgressionDistribution.FLAT);

        outbreak.setPopulation(population);
        outbreak.setStandardProperties(properties);
        outbreak.setInitialCaseReader(initialCaseReader);
        outbreak.setDiseaseProperties(diseaseProperties);

        outbreak.generateInitialInfection();

        population = getPopulationViaReflection();

        Set<Case> exposures = population.values().stream().filter(caze -> caze.status() == EXPOSED).collect(Collectors.toSet());

        Set<Integer> set = exposures.stream().map(Case::id).collect(Collectors.toSet());


        int max = set.stream().max(Comparator.naturalOrder()).orElseThrow();
        int min = set.stream().min(Comparator.naturalOrder()).orElseThrow();

        Assert.assertEquals(infected, set.size());
        Assert.assertEquals(popSize, population.size());
        Assert.assertTrue(max < population.size());
        Assert.assertTrue(min >= 0);

    }

    @Test
    public void runToCompletionAllContact() throws FileNotFoundException {
        int popSize = 100;
        int infections = 1 + popSize / 10;

        DiseaseProperties d = TestUtils.diseaseProperties();
        outbreak.setDiseaseProperties(d);

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

        Map<Integer, List<ContactRecord>> contacts = createContactRecords(500, population);

        outbreak.setPopulation(population);
        outbreak.setStandardProperties(properties);
        outbreak.setInitialCaseReader(initialCaseReader);
        outbreak.setDiseaseProperties(d);
        outbreak.setContactRecords(contacts);
        outbreak.generateInitialInfection();

        long susceptible = population.values().stream().map(Case::status).filter(status -> status == SUSCEPTIBLE).count();
        outbreak.runToCompletion();

        long susceptiblePost = getPopulationViaReflection().values().stream().map(Case::status).filter(status -> status == SUSCEPTIBLE).count();

        Assert.assertTrue(susceptiblePost < susceptible);
        Assert.assertThat(logSpy.getOutput(), containsString("Not all contact data will be used"));
        Assert.assertThat(logSpy.getOutput(), containsString("There are no active cases and the random infection rate is zero."));
        Assert.assertThat(logSpy.getOutput(), containsString("Exiting as solution is stable."));

    }


    @Test
    public void runToCompletionSmallContact() throws FileNotFoundException {
        int popSize = 100;
        int infections = 1 + popSize / 10;

        DiseaseProperties d = TestUtils.diseaseProperties();
        outbreak.setDiseaseProperties(d);

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
        when(properties.steadyState()).thenReturn(true);

        Map<Integer, List<ContactRecord>> contacts = createContactRecords(5, population);

        outbreak.setPopulation(population);
        outbreak.setStandardProperties(properties);
        outbreak.setInitialCaseReader(initialCaseReader);
        outbreak.setDiseaseProperties(d);
        outbreak.setContactRecords(contacts);
        outbreak.generateInitialInfection();

        long susceptible = population.values().stream().map(Case::status).filter(status -> status == SUSCEPTIBLE).count();
        outbreak.runToCompletion();

        long susceptiblePost = getPopulationViaReflection().values().stream().map(Case::status).filter(status -> status == SUSCEPTIBLE).count();

        Assert.assertTrue(susceptiblePost < susceptible);
        Assert.assertThat(logSpy.getOutput(), containsString("Steady state solution reached at t=12"));
        Assert.assertThat(logSpy.getOutput(), containsString("Exiting early."));

    }


    @Test
    public void evaluateContactAlerted() {

        DiseaseProperties d = mock(DiseaseProperties.class);
        when(d.progressionDistribution()).thenReturn(ProgressionDistribution.FLAT);
        when(d.exposureThreshold()).thenReturn(101d);
        outbreak.setDiseaseProperties(d);

        Map<Integer, Case> population = new HashMap<>();
        Case diseased = new Case(Mockito.mock(Human.class));
        ReflectionTestUtils.setField(diseased, "status", INFECTED);

        int diseasedId = 0;
        int clearId = 1;
        Case clear = new Case(Mockito.mock(Human.class));

        ReflectionTestUtils.setField(clear, "alertStatus", ALERTED);
        population.put(diseasedId, diseased);
        population.put(clearId, clear);
        outbreak.setPopulation(population);

        ContactRecord contact = ImmutableContactRecord.builder().from(0).to(1).time(0).weight(100).build();
        int time = 0;

        outbreak.evaluateContact(time, contact);

        population = getPopulationViaReflection();

        clear = population.get(clearId);

        Assert.assertEquals(SUSCEPTIBLE, clear.status());
        Assert.assertEquals(Case.getDefault(), clear.exposedBy());
        Assert.assertThat(logSpy.getOutput(), containsString("Skipping contact due to threshold"));

    }


    @Test
    public void evaluateContact() {

        DiseaseProperties d = mock(DiseaseProperties.class);
        when(d.progressionDistribution()).thenReturn(ProgressionDistribution.FLAT);
        outbreak.setDiseaseProperties(d);

        Map<Integer, Case> population = new HashMap<>();
        Case diseased = new Case(Mockito.mock(Human.class));
        ReflectionTestUtils.setField(diseased, "status", INFECTED);

        int diseasedId = 0;
        int clearId = 1;
        Case clear = new Case(Mockito.mock(Human.class));

        population.put(diseasedId, diseased);
        population.put(clearId, clear);
        outbreak.setPopulation(population);

        ContactRecord contact = ImmutableContactRecord.builder().from(0).to(1).time(0).weight(100).build();
        int time = 0;
        outbreak.evaluateContact(time, contact);

        population = getPopulationViaReflection();

        clear = population.get(clearId);

        Assert.assertEquals(EXPOSED, clear.status());
        Assert.assertEquals(diseasedId, clear.exposedBy());

    }


    @Test
    public void runContactDataWithRandoms() {
        DiseaseProperties d = mock(DiseaseProperties.class);
        when(d.progressionDistribution()).thenReturn(ProgressionDistribution.FLAT);
        outbreak.setDiseaseProperties(d);

        int days = 11;
        int popSize = 500;
        double randomInfection = 0.1;

        Map<Integer, Case> population = new HashMap<>();

        for (int i = 0; i < popSize; i++) {
            Human human = mock(Human.class);
            Case thisCase = new Case(human);
            population.put(i, thisCase);
        }

        Map<Integer, List<ContactRecord>> contacts = createContactRecords(days, population);

        outbreak.setPopulation(population);
        outbreak.setContactRecords(contacts);

        outbreak.runContactData(days - 1, randomInfection);

        long sus = population.values().stream().map(Case::status).filter(status -> status == SUSCEPTIBLE).count();

        // we expect this to roughly follow an exp decay
        double test = popSize * Math.exp(-randomInfection * days);
        Assert.assertEquals(test / (double) popSize, sus / (double) popSize, 0.1);


    }

    @Test
    public void runContactDataWithoutRandoms() {
        DiseaseProperties d = mock(DiseaseProperties.class);
        when(d.progressionDistribution()).thenReturn(ProgressionDistribution.FLAT);
        outbreak.setDiseaseProperties(d);

        int days = 11;
        int popSize = 500;
        double randomInfection = 0.0;

        Map<Integer, Case> population = new HashMap<>();

        for (int i = 0; i < popSize; i++) {
            Human human = mock(Human.class);
            Case thisCase = new Case(human);
            population.put(i, thisCase);
        }

        Map<Integer, List<ContactRecord>> contacts = createContactRecords(days, population);

        outbreak.setPopulation(population);
        outbreak.setContactRecords(contacts);

        outbreak.runContactData(days - 1, randomInfection);

        Assert.assertThat(logSpy.getOutput(), containsString("There are no active cases and the random infection rate is zero."));
        Assert.assertThat(logSpy.getOutput(), containsString("Exiting as solution is stable."));

    }

    private Map<Integer, List<ContactRecord>> createContactRecords(int days, Map<Integer, Case> population) {
        Map<Integer, List<ContactRecord>> contacts = new HashMap<>();
        for (int i = 0; i < days; i++) {

            List<ContactRecord> dailyContacts = new ArrayList<>();
            for (int j = 0; j < random.nextInt(population.size()); j++) {

                int personA = random.nextInt(population.size());
                int personB = random.nextInt(population.size());
                int weight = random.nextInt(100);

                dailyContacts.add(ImmutableContactRecord.builder().from(personA).to(personB).weight(weight).time(i).build());

            }
            contacts.put(i, dailyContacts);
        }
        return contacts;
    }


    @Test
    public void runToSteadyState() {
        Map<Integer, Case> population = new HashMap<>();

        for (int i = 0; i < 500; i++) {
            Human human = mock(Human.class);
            Case thisCase = new Case(human);
            population.put(i, thisCase);
        }
        outbreak.setPopulation(population);
        outbreak.runToSteadyState(10, 100);

        Assert.assertThat(logSpy.getOutput(), containsString("Steady state solution reached at t=10"));
        Assert.assertThat(logSpy.getOutput(), containsString("Exiting early."));


    }

    @Test
    public void updatePopulationState() {

        Map<Integer, Case> population = new HashMap<>();
        double randomInfection = 0.1d;
        int time = 0;

        DiseaseProperties d = mock(DiseaseProperties.class);
        when(d.progressionDistribution()).thenReturn(ProgressionDistribution.FLAT);
        outbreak.setDiseaseProperties(d);


        for (int i = 0; i < 500; i++) {
            Human human = mock(Human.class);
            Case thisCase = new Case(human);
            population.put(i, thisCase);
        }

        long count = population.values().stream().map(Case::status).filter(status -> status != SUSCEPTIBLE).count();
        outbreak.setPopulation(population);
        outbreak.updatePopulationState(time, randomInfection);

        population = getPopulationViaReflection();


        long count2 = population.values().stream().map(Case::status).filter(status -> status != SUSCEPTIBLE).count();


        // at t =0 there are no random infection
        Assert.assertEquals(count, count2);

        time++;
        outbreak.updatePopulationState(time, randomInfection);

        population = getPopulationViaReflection();

        count2 = population.values().stream().map(Case::status).filter(status -> status != SUSCEPTIBLE).count();

        Assert.assertTrue(count2 > 0);

        // Check random exposures are roughly in line with randomInfectionRate
        Assert.assertEquals((double) count2 / (double) (population.size()), randomInfection, .1);


    }

    @Test
    public void alertPopulation() {

        int time = 0;
        Map<Integer, Case> population = new HashMap<>();
        Set<Integer> alerts = new HashSet<>();
        int numVirusStatus = values().length;
        int numAlertStatus = AlertStatus.values().length;
        int unableToAlert = 0;
        for (int i = 0; i < 10000; i++) {
            VirusStatus virusStatus = VirusStatus.values()[random.nextInt(numVirusStatus)];
            AlertStatus alertStatus = AlertStatus.values()[random.nextInt(numAlertStatus)];

            if (alertStatus != AlertStatus.NONE || virusStatus == DEAD) {
                unableToAlert++;
            } else {
                alerts.add(i);
            }

            Human human = mock(Human.class);
            Case thisCase = new Case(human);
            ReflectionTestUtils.setField(thisCase, "alertStatus", alertStatus);
            ReflectionTestUtils.setField(thisCase, "status", virusStatus);
            when(thisCase.id()).thenReturn(i);

            population.put(i, thisCase);
        }
        outbreak.setPopulation(population);

        long preAlerted = population.values().stream().map(Case::alertStatus).filter(alertStatus -> alertStatus != AlertStatus.NONE).count();
        outbreak.alertPopulation(alerts, time);


        population = getPopulationViaReflection();

        long none2 = population.values().stream().map(Case::alertStatus).filter(alertStatus -> alertStatus == AlertStatus.NONE).count();

        Assert.assertNotNull(population);
        long updateNextTime = population.values().stream().mapToInt(Case::nextAlertStatusChange).filter(i -> i > time).count();

        Assert.assertEquals(population.size() - unableToAlert, alerts.size());
        Assert.assertEquals(population.size() - none2, preAlerted);
        Assert.assertEquals(alerts.size(), updateNextTime);


    }

    @Test
    public void calculateDailyStatistics() {
        int time = 0;
        int numStatus = values().length;
        int inactiveCases = 0;
        Map<Integer, Case> population = new HashMap<>();
        for (int i = 0; i < 500; i++) {
            VirusStatus status = VirusStatus.values()[random.nextInt(numStatus)];
            if (status == SUSCEPTIBLE || status == RECOVERED || status == DEAD) inactiveCases++;
            Case thisCase = Mockito.mock(Case.class);
            when(thisCase.status()).thenReturn(status);
            when(thisCase.id()).thenReturn(i);

            population.put(i, thisCase);
        }

        outbreak.setPopulation(population);

        int activeCases = outbreak.calculateDailyStatistics(time);
        Assert.assertEquals(population.size(), activeCases + inactiveCases);

    }

    private Map<Integer, Case> getPopulationViaReflection() {
        Map<Integer, Case> population = (Map<Integer, Case>) ReflectionTestUtils.getField(outbreak, "population");
        Assert.assertNotNull(population);
        Assert.assertTrue(population.size() > 0);

        return population;
    }

}