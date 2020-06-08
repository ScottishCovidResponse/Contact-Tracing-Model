package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.*;
import org.mockito.Mockito;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.InputFiles;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppConfigTest {

    @Rule
    public LogSpy appender = new LogSpy();
    AppConfig appConfig;

    @Before
    public void setUp() {
        appConfig = new AppConfig();
    }

    @After
    public void tearDown() {
        appConfig = null;
    }

    @Test
    public void standardProperties() throws ConfigurationException {

        StandardProperties standardProperties = appConfig.standardProperties();

        Assert.assertNotNull(standardProperties);
//        Assert.assertFalse(standardProperties.contactsFile().isEmpty());
        Assert.assertNotNull(standardProperties.steadyState());
        Assert.assertTrue(standardProperties.initialExposures() > 0);
        Assert.assertTrue(standardProperties.populationSize() > 0);
        Assert.assertTrue(standardProperties.seed() >= 0);
        Assert.assertTrue(standardProperties.timeLimit() > 0);
        Assert.assertTrue(standardProperties.populationSize() > standardProperties.initialExposures());

    }

    @Test(expected = ConfigurationException.class)
    public void standardPropertiesError() throws ConfigurationException, IOException {

        String file = File.createTempFile("temp", "file").getAbsolutePath();
        try {
            InputFiles input = mock(InputFiles.class);
            when(input.runSettings()).thenReturn(file);
            appConfig = mock(AppConfig.class);
            when(appConfig.getReader(anyString())).thenThrow(new FileNotFoundException());
            when(appConfig.standardProperties()).thenCallRealMethod();
            when(appConfig.inputFiles()).thenReturn(input);
            appConfig.standardProperties();
        } catch (ConfigurationException e) {
            Assert.assertThat(appender.getOutput(), containsString("An error occurred while parsing the run properties at " + file));
            throw e;
        }


    }


    @Test
    public void diseaseProperties() throws ConfigurationException {

        DiseaseProperties diseaseProperties = appConfig.diseaseProperties();

        Assert.assertNotNull(diseaseProperties);

        Assert.assertTrue(diseaseProperties.meanTimeToInfectious() > 0);
        Assert.assertTrue(diseaseProperties.meanTimeToInfected() > 0);
        Assert.assertTrue(diseaseProperties.meanTimeToFinalState() > 0);
        Assert.assertTrue(diseaseProperties.maxTimeToInfectious() > 0);
        Assert.assertTrue(diseaseProperties.maxTimeToInfected() > 0);
        Assert.assertTrue(diseaseProperties.maxTimeToFinalState() > 0);
        Assert.assertTrue(diseaseProperties.meanTestTime() > 0);
        Assert.assertTrue(diseaseProperties.maxTestTime() > 0);
        Assert.assertTrue(diseaseProperties.testAccuracy() > 0);
        Assert.assertTrue(diseaseProperties.exposureTuning() > 0);
        Assert.assertTrue(diseaseProperties.exposureThreshold() > 0);
        Assert.assertTrue(diseaseProperties.randomInfectionRate() >= 0);
        Assert.assertNotNull(diseaseProperties.progressionDistribution());


    }

    @Test(expected = ConfigurationException.class)
    public void diseasePropertiesError() throws IOException, ConfigurationException {
        String file = File.createTempFile("temp", "file").getAbsolutePath();
        try {
            InputFiles input = mock(InputFiles.class);
            when(input.diseaseSettings()).thenReturn(file);
            appConfig = mock(AppConfig.class);
            when(appConfig.getReader(anyString())).thenThrow(new FileNotFoundException());
            when(appConfig.diseaseProperties()).thenCallRealMethod();
            when(appConfig.inputFiles()).thenReturn(input);
            appConfig.diseaseProperties();
        } catch (ConfigurationException e) {
            Assert.assertThat(appender.getOutput(), containsString("An error occurred while parsing the disease properties at " + file));
            throw e;
        }

    }

    @Test
    public void populationProperties() throws ConfigurationException {

        PopulationProperties populationProperties = appConfig.populationProperties();
        Assert.assertTrue(populationProperties.genderBalance() > 0);
        Assert.assertNotNull(populationProperties.populationAges());
        Assert.assertNotNull(populationProperties.populationDistribution());
        Assert.assertEquals(populationProperties.populationAges().size(), populationProperties.populationDistribution().size());

    }

    @Test(expected = ConfigurationException.class)
    public void populationPropertiesError() throws IOException, ConfigurationException {
        String file = File.createTempFile("temp", "file").getAbsolutePath();
        try {
            InputFiles input = mock(InputFiles.class);
            when(input.populationSettings()).thenReturn(file);
            appConfig = mock(AppConfig.class);
            when(appConfig.getReader(anyString())).thenThrow(new FileNotFoundException());
            when(appConfig.populationProperties()).thenCallRealMethod();
            when(appConfig.inputFiles()).thenReturn(input);
            appConfig.populationProperties();
        } catch (ConfigurationException e) {
            Assert.assertThat(appender.getOutput(), containsString("An error occurred while parsing the population properties at " + file));
            throw e;
        }

    }

    @Test
    public void randomDataGeneratorNoArgs() throws ConfigurationException {

        RandomDataGenerator r = appConfig.randomDataGenerator(Optional.empty());
        int seed = appConfig.standardProperties().seed();
        Assert.assertNotNull(r);
        Assert.assertThat(appender.getOutput(), containsString("Additional Seed information not provided, defaulting to " + seed));
    }

    @Test
    public void randomDataGeneratorWithArgs() throws ConfigurationException {

        Random random = TestUtils.getRandom();

        int arg = random.nextInt(10);
        String[] args = {arg + ""};
        System.out.println(args[0]);

        RandomDataGenerator r = appConfig.randomDataGenerator(Optional.of(args));
        int seed = appConfig.standardProperties().seed();
        Assert.assertNotNull(r);
        Assert.assertThat(appender.getOutput(), containsString("Additional Seed information provided, the seed will be " + (seed + arg)));
    }

    @Test(expected = ConfigurationException.class)
    public void randomDataGeneratorWithInvalidArgs() throws ConfigurationException {

        String[] args = {"seed"};
        try {
            appConfig.randomDataGenerator(Optional.of(args));
        } catch (ConfigurationException e) {
            Assert.assertThat(appender.getOutput(), containsString("An error occurred while creating the random generator. The command line arg, \"" + args[0] + "\", could not be parsed as an integer."));
            throw e;
        }
    }

    @Test(expected = ConfigurationException.class)
    public void randomDataGeneratorWithInvalidSystemProperties() throws ConfigurationException {

        AppConfig appConfig = Mockito.mock(AppConfig.class);
        when(appConfig.randomDataGenerator(any())).thenCallRealMethod();
        try {
            appConfig.randomDataGenerator(Optional.empty());

        } catch (ConfigurationException e) {
            Assert.assertThat(appender.getOutput(), containsString("An error occurred while creating the random generator. This is likely due to an error in Standard Properties"));
            throw e;
        }
    }


}