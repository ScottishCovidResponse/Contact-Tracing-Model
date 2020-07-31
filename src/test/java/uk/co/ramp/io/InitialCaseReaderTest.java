package uk.co.ramp.io;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.LogSpy;
import uk.co.ramp.TestUtils;
import uk.co.ramp.io.types.InputFiles;
import uk.co.ramp.io.types.StandardProperties;

public class InitialCaseReaderTest {

  @Rule public LogSpy logSpy = new LogSpy();
  private InitialCaseReader caseReader;
  private RandomDataGenerator generator;
  private StandardProperties standardProperties;
  private InputFiles inputFiles;

  @Before
  public void setup() {

    generator = TestUtils.dataGenerator();
    standardProperties = Mockito.mock(StandardProperties.class);
    inputFiles = Mockito.mock(InputFiles.class);

    when(standardProperties.initialExposures()).thenReturn(10);
    when(standardProperties.populationSize()).thenReturn(100);
  }

  @Test
  public void testValidFile() {

    String resource = InitialCaseReaderTest.class.getResource("Long.csv").getFile();

    when(inputFiles.initialExposures()).thenReturn(resource);

    caseReader = new InitialCaseReader(generator, standardProperties, inputFiles);
    Set<Integer> cases = caseReader.getCases();

    Assert.assertEquals(10, cases.size());
    Assert.assertThat(logSpy.getOutput(), containsString("Read in a list of 10 entries"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString(
            "After removing duplicates and limiting to values below the population size, 100, there are 10 entries"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("The set of initial cases is 10 long, when 10 was requested"));
    Assert.assertThat(
        logSpy.getOutput(), containsString("The correct number of entries have been read in."));
    Assert.assertThat(
        logSpy.getOutput(), containsString("The set is 10 long and complies with the input"));
  }

  @Test
  public void testInvalidFile() {

    String resource = "This file doesn't exist";

    when(inputFiles.initialExposures()).thenReturn(resource);

    caseReader = new InitialCaseReader(generator, standardProperties, inputFiles);
    Set<Integer> cases = caseReader.getCases();

    Assert.assertEquals(10, cases.size());
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("An IOException was thrown while populating the initial cases."));
    Assert.assertThat(
        logSpy.getOutput(), containsString("This file doesn't exist"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("The set of initial cases is 0 long, when 10 was requested"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString(
            "The list initial infections contains 0 entries, but the input has specified 10."));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("The additional 10 infections will be randomly assigned."));
    Assert.assertThat(
        logSpy.getOutput(), containsString("The set is 10 long and complies with the input"));
  }

  @Test
  public void testShortFile() {

    String resource = InitialCaseReaderTest.class.getResource("Short.csv").getFile();

    when(inputFiles.initialExposures()).thenReturn(resource);

    caseReader = new InitialCaseReader(generator, standardProperties, inputFiles);
    Set<Integer> cases = caseReader.getCases();

    Assert.assertEquals(10, cases.size());
    Assert.assertThat(logSpy.getOutput(), containsString("Read in a list of 5 entries"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString(
            "After removing duplicates and limiting to values below the population size, 100, there are 5 entries"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("The set of initial cases is 5 long, when 10 was requested"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString(
            "The list initial infections contains 5 entries, but the input has specified 10."));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("The additional 5 infections will be randomly assigned."));
  }

  @Test
  public void testLongFile() {

    String resource = InitialCaseReaderTest.class.getResource("Long.csv").getFile();

    when(inputFiles.initialExposures()).thenReturn(resource);
    when(standardProperties.initialExposures()).thenReturn(5);
    caseReader = new InitialCaseReader(generator, standardProperties, inputFiles);
    Set<Integer> cases = caseReader.getCases();

    Assert.assertEquals(5, cases.size());
    Assert.assertThat(logSpy.getOutput(), containsString("Read in a list of 10 entries"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString(
            "After removing duplicates and limiting to values below the population size, 100, there are 10 entries"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("The set of initial cases is 10 long, when 5 was requested"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString(
            "The list of initial infections contains 10 cases, but the limit has been set to 5"));
    Assert.assertThat(
        logSpy.getOutput(), containsString("The list will be trimmed to a size of 10"));
  }

  @Test
  public void testDuplicateAndOutOfRangeRemoval() {

    String resource = InitialCaseReaderTest.class.getResource("Duplicates.csv").getFile();

    when(inputFiles.initialExposures()).thenReturn(resource);
    when(standardProperties.initialExposures()).thenReturn(10);
    caseReader = new InitialCaseReader(generator, standardProperties, inputFiles);
    Set<Integer> cases = caseReader.getCases();

    Assert.assertEquals(10, cases.size());
    Assert.assertThat(logSpy.getOutput(), containsString("Read in a list of 11 entries"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString(
            "After removing duplicates and limiting to values below the population size, 100, there are 2 entries"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("The set of initial cases is 2 long, when 10 was requested"));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString(
            "The list initial infections contains 2 entries, but the input has specified 10."));
    Assert.assertThat(
        logSpy.getOutput(),
        containsString("The additional 8 infections will be randomly assigned."));
  }

  @Test(expected = AssertionError.class)
  public void lengthAssertion() throws IOException {

    String resource = InitialCaseReaderTest.class.getResource("Long.csv").getFile();

    when(inputFiles.initialExposures()).thenReturn(resource);
    when(standardProperties.initialExposures()).thenReturn(5);

    caseReader = Mockito.mock(InitialCaseReader.class);

    ReflectionTestUtils.setField(caseReader, "cases", new HashSet<>());
    ReflectionTestUtils.setField(caseReader, "standardProperties", standardProperties);

    doCallRealMethod().when(caseReader).tryPopulateCases(any());
    doCallRealMethod().when(caseReader).read(any());
    doNothing().when(caseReader).fillOrTrimSet();

    caseReader.tryPopulateCases(inputFiles);
    Assert.fail();
  }
}
