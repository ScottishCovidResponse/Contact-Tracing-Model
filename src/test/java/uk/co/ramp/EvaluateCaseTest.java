package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.people.*;
import uk.co.ramp.utilities.ForbiddenAccessException;

import java.util.Optional;
import java.util.Random;

import static org.hamcrest.CoreMatchers.containsString;
import static uk.co.ramp.people.VirusStatus.*;

public class EvaluateCaseTest {

    @Rule
    public LogSpy logSpy = new LogSpy();
    Random r = TestUtils.getRandom();
    private EvaluateCase evaluateCase;
    private Case aCase;

    @Before
    public void setup() throws ConfigurationException {

        AppConfig appConfig = new AppConfig();
        Human human = ImmutableHuman.builder().health(0.5).gender(Gender.MALE).age(50).compliance(0.5).id(0).build();
        aCase = new Case(human);
        DiseaseProperties d = appConfig.diseaseProperties();

        RandomDataGenerator r = appConfig.randomDataGenerator(Optional.empty());


        evaluateCase = new EvaluateCase(aCase, d, r);
    }


    @Test
    public void initialExposure() {

        int t = 0;
        Assert.assertEquals(SUSCEPTIBLE, aCase.status());
        Assert.assertEquals(Case.getDefault(), aCase.exposedTime());
        Assert.assertEquals(Case.getDefault(), aCase.exposedBy());

        evaluateCase.initialExposure(EXPOSED, t);

        Assert.assertEquals(EXPOSED, aCase.status());
        Assert.assertEquals(-1, aCase.exposedTime());
        Assert.assertEquals(Case.getInitial(), aCase.exposedBy());

    }

    @Test(expected = ForbiddenAccessException.class)
    public void invalidInitialExposure() {

        int t = 10;
        Assert.assertEquals(SUSCEPTIBLE, aCase.status());
        Assert.assertEquals(Case.getDefault(), aCase.exposedTime());
        Assert.assertEquals(Case.getDefault(), aCase.exposedBy());
        try {
            evaluateCase.initialExposure(EXPOSED, t);
        } catch (ForbiddenAccessException e) {
            Assert.assertThat(logSpy.getOutput(), containsString("Unable to set an initial exposure at t > 0"));
            throw e;
        }

    }


    @Test
    public void updateVirusStatusWithInfecter() {

        int t = r.nextInt(100);
        int infecter = r.nextInt(100);

        Assert.assertEquals(SUSCEPTIBLE, aCase.status());
        Assert.assertEquals(Case.getDefault(), aCase.exposedTime());
        Assert.assertEquals(Case.getDefault(), aCase.exposedBy());

        evaluateCase.updateVirusStatus(EXPOSED, t, infecter);

        Assert.assertEquals(EXPOSED, aCase.status());
        Assert.assertEquals(t, aCase.exposedTime());
        Assert.assertEquals(infecter, aCase.exposedBy());

        t += r.nextInt(10);

        evaluateCase.updateVirusStatus(EXPOSED_2, t, infecter);
        Assert.assertEquals(EXPOSED_2, aCase.status());
        t += r.nextInt(10);
        evaluateCase.updateVirusStatus(INFECTED, t, infecter);
        Assert.assertEquals(INFECTED, aCase.status());
        t += r.nextInt(10);
        evaluateCase.updateVirusStatus(RECOVERED, t, infecter);

        Assert.assertEquals(RECOVERED, aCase.status());
        Assert.assertEquals(t, aCase.exposedTime());
        Assert.assertEquals(infecter, aCase.exposedBy());


    }

    @Test
    public void randomExposure() {
        int t = r.nextInt(10);
        evaluateCase.randomExposure(t);
        Assert.assertEquals(Case.getRandomInfection(), aCase.exposedBy());
        Assert.assertEquals(t, aCase.exposedTime());
    }

    @Test(expected = InvalidStatusTransitionException.class)
    public void randomExposureWrongStatus() {
        int t = r.nextInt(10);

        aCase.setStatus(EXPOSED);
        try {
            evaluateCase.randomExposure(t);
        } catch (InvalidStatusTransitionException e) {
            Assert.assertThat(logSpy.getOutput(), containsString("The person with id: 0 should not be able to transition from EXPOSED to EXPOSED"));
            throw e;
        }
    }

    @Test
    public void testUpdateVirusStatus() {
    }

    @Test
    public void checkActionsAtTimestep() {
    }


    @Test
    public void getDistributionValue() {


        for (int i = 0; i < 1000; i++) {
            System.out.println(evaluateCase.getDistributionValue(5, 14));
        }


    }
}