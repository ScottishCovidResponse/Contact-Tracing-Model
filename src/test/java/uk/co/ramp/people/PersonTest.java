package uk.co.ramp.people;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import uk.co.ramp.ContactRunner;
import uk.co.ramp.LogAppender;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ProgressionDistribution;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.co.ramp.people.VirusStatus.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "uk.co.ramp.ContactRunner")
@PowerMockIgnore("javax.management.*")
public class PersonTest {

    private final static double DELTA = 1e-6;
    private final RandomDataGenerator rnd = new RandomDataGenerator();
    private Person person;
    private DiseaseProperties diseaseProperties;
    private int id;
    private int age;
    private Gender gender;
    private double compliance;
    private double health;

    @Rule
    public LogAppender appender = new LogAppender(Person.class);

    @Before
    public void setUp() throws Exception {

        mockStatic(ContactRunner.class);
        diseaseProperties = new DiseaseProperties(3, 7, 0, 90, ProgressionDistribution.FLAT);
        when(ContactRunner.getDiseaseProperties()).thenReturn(diseaseProperties);
        when(ContactRunner.getRng()).thenReturn(rnd);
        id = rnd.nextInt(0, 100);
        age = rnd.nextInt(0, 100);
        gender = rnd.nextUniform(0, 1) > 0.5 ? Gender.FEMALE : Gender.MALE;
        compliance = rnd.nextUniform(0, 1);
        health = rnd.nextUniform(0, 1);


        person = new Person(id, age, gender, compliance, health);


    }

    @Test
    public void testBasics() {

        Assert.assertEquals(id, person.getId());
        Assert.assertEquals(age, person.getAge());
        Assert.assertEquals(gender, person.getGender());
        Assert.assertEquals(compliance, person.getCompliance(), DELTA);
        Assert.assertEquals(health, person.getHealth(), DELTA);

    }


    @Test
    public void defaultSusceptible() {
        Assert.assertEquals(SUSCEPTIBLE, person.getStatus());
    }


    @Test
    public void updateStatus() {
        Assert.assertEquals(SUSCEPTIBLE, person.getStatus());
        person.updateStatus(EXPOSED, 0);
        Assert.assertEquals(EXPOSED, person.getStatus());
        person.updateStatus(INFECTED, 0);
        Assert.assertEquals(INFECTED, person.getStatus());
        person.updateStatus(RECOVERED, 0);
        Assert.assertEquals(RECOVERED, person.getStatus());
    }

    @Test
    public void testExposedBy() {

        Assert.assertEquals(SUSCEPTIBLE, person.getStatus());
        Assert.assertEquals(-1, person.getExposedBy());
        int exposer = rnd.nextInt(0, 1000);
        person.updateStatus(EXPOSED, 0, exposer);

        Assert.assertEquals(EXPOSED, person.getStatus());
        Assert.assertEquals(exposer, person.getExposedBy());

    }


    @Test
    public void getNextStatusChange() {
        Assert.assertEquals(SUSCEPTIBLE, person.getStatus());
        Assert.assertEquals(-1, person.getExposedBy());
        int exposer = rnd.nextInt(0, 1000);
        int timeNow = rnd.nextInt(0, 100);
        person.updateStatus(EXPOSED, timeNow, exposer);

        Assert.assertEquals(EXPOSED, person.getStatus());
        Assert.assertTrue(person.getNextStatusChange() > timeNow);

    }

    @Test(expected = InvalidStatusTransitionException.class)
    public void checkTime() {

        int time = rnd.nextInt(0, 100);
        int flatTransition = (int) diseaseProperties.getMeanTimeToInfected();

        person.updateStatus(EXPOSED, time - flatTransition);

        person.checkTime(time);

        flatTransition = (int) diseaseProperties.getMeanTimeToRecovered();
        Assert.assertEquals(INFECTED, person.getStatus());
        Assert.assertEquals(time + flatTransition, person.getNextStatusChange());


        time += flatTransition;
        person.checkTime(time);
        Assert.assertEquals(RECOVERED, person.getStatus());
        Assert.assertEquals(-1, person.getNextStatusChange());


        person.updateStatus(SUSCEPTIBLE, ++time);
        Whitebox.setInternalState(person, "nextStatusChange", time);

        try {
            person.checkTime(time);
            Assert.fail();
        } catch (InvalidStatusTransitionException e) {
            Assert.assertThat(appender.getOutput(), containsString(String.format("Changing status from RECOVERED for person.id %d is not a valid transition", id)));
            throw e;
        }
    }

    @Test
    public void notNow() {
        int time = rnd.nextInt(0, 100);
        int flatTransition = (int) diseaseProperties.getMeanTimeToInfected();

        person.updateStatus(EXPOSED, time - flatTransition);
        person.checkTime(time - 10);

        // event times don't match, so nothing happens
        Assert.assertEquals("", appender.getOutput());
    }

    @Test
    public void randomExposure() {
        int time = rnd.nextInt(0, 1000);
        person.randomExposure(time);
        Assert.assertThat(appender.getOutput(), containsString(String.format("Person with id: %d has been randomly exposed at time %d", id, time)));
    }


    @Test(expected = InvalidStatusTransitionException.class)
    public void invalidExposure() {
        int time = rnd.nextInt(0, 1000);

        System.out.println(person.getStatus());
        person.updateStatus(EXPOSED, 0);
        time = person.getNextStatusChange();

        person.checkTime(time);
        System.out.println(person.getStatus());

        try {
            person.randomExposure(time);
        } catch (InvalidStatusTransitionException e) {
            String message = String.format("The person with id: %d should not be able to transition from %s to %s", person.getId(), person.getStatus(), EXPOSED);
            Assert.assertThat(appender.getOutput(), containsString(message));
            throw e;
        }

    }

    @Test
    public void getGaussianDistributionValue() {
        int n = 10000;
        double mean = 7;
        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            results.add(person.getDistributionValue(mean, ProgressionDistribution.GAUSSIAN));
        }

        int out = results.stream().mapToInt(Integer::intValue).sum();

        System.out.println(out / (double) n);
        System.out.println(Math.sqrt(2d / (double) n));

        Assert.assertEquals(mean, out / (double) n, 0.01 * mean);


    }

    @Test
    public void getLinearDistributionValue() {
        int n = 10000;
        double mean = 7;
        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            results.add(person.getDistributionValue(mean, ProgressionDistribution.LINEAR));
        }

        int out = results.stream().mapToInt(Integer::intValue).sum();
        Assert.assertEquals(mean, out / (double) n, 0.01 * mean);
    }

    @Test
    public void getExponentialDistributionValue() {
        int n = 10000;
        double mean = 7;

        RandomDataGenerator randomGenerator = new RandomDataGenerator();

        List<Integer> results = new ArrayList<>();
        List<Double> results2 = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            results.add(person.getDistributionValue(mean, ProgressionDistribution.EXPONENTIAL));
            results2.add(Math.min(randomGenerator.nextExponential(mean), 14));
        }

        int out = results.stream().mapToInt(Integer::intValue).sum();
        int out2 = results2.stream().mapToInt(i -> (int) Math.round(i)).sum();


        // TODO: this is a little weak... need to speak to Louise/Sibyll regarding number capping.
        Assert.assertEquals(out2 / (double) n, out / (double) n, 0.05 * mean);

    }


}