package uk.co.ramp.people;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.co.ramp.ContactRunner;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ProgressionDistribution;

import java.util.Random;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.co.ramp.people.VirusStatus.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "uk.co.ramp.ContactRunner")
@PowerMockIgnore("javax.management.*")
public class PersonTest {

    private final static double DELTA = 1e-6;
    private final Random rnd = new Random();
    private Person person;
    private DiseaseProperties diseaseProperties;
    private int id;
    private int age;
    private Gender gender;
    private double compliance;
    private double health;

    @Before
    public void setUp() throws Exception {

        mockStatic(ContactRunner.class);
        diseaseProperties = new DiseaseProperties(3, 7, 0, 90, ProgressionDistribution.FLAT);
        when(ContactRunner.getDiseaseProperties()).thenReturn(diseaseProperties);

        id = rnd.nextInt(100);
        age = rnd.nextInt(100);
        gender = rnd.nextBoolean() ? Gender.FEMALE : Gender.MALE;
        compliance = rnd.nextDouble();
        health = rnd.nextDouble();


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
        int exposer = rnd.nextInt(1000);
        person.updateStatus(EXPOSED, 0, exposer);

        Assert.assertEquals(EXPOSED, person.getStatus());
        Assert.assertEquals(exposer, person.getExposedBy());

    }


    @Test
    public void getNextStatusChange() {
        Assert.assertEquals(SUSCEPTIBLE, person.getStatus());
        Assert.assertEquals(-1, person.getExposedBy());
        int exposer = rnd.nextInt(1000);
        int timeNow = rnd.nextInt(100);
        person.updateStatus(EXPOSED, timeNow, exposer);

        Assert.assertEquals(EXPOSED, person.getStatus());
        Assert.assertTrue(person.getNextStatusChange() > timeNow);

    }

    @Test
    public void checkTime() {

        int time = rnd.nextInt(100);
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

        person.checkTime(time);


    }

    @Test
    public void randomExposure() {
    }

    @Test
    public void getDistributionValue() {
    }
}