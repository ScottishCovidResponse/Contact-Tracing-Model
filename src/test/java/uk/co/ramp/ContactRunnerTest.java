package uk.co.ramp;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.*;
import org.springframework.context.ApplicationContext;
import uk.co.ramp.event.CompletionEventListGroup;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.io.CompartmentWriter;
import uk.co.ramp.io.ContactReader;
import uk.co.ramp.io.csv.CsvException;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.InputFiles;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.PopulationGenerator;
import uk.ramp.api.StandardApi;

public class ContactRunnerTest {

  @Rule public final LogSpy logSpy = new LogSpy();
  private ContactRunner runner;
  private ApplicationContext applicationContext;

  @Before
  public void setUp() {
    applicationContext = mock(ApplicationContext.class);
    runner = new ContactRunner();
    runner.setApplicationContext(applicationContext);
    StandardApi dataPipelineApi = mock(StandardApi.class);
    runner.setDataPipelineApi(dataPipelineApi);
  }

  @After
  public void tearDown() {
    runner = null;
    applicationContext = null;
  }

  @Test
  public void run() throws IOException {

    // Stub classes
    PopulationGenerator populationGenerator = mock(PopulationGenerator.class);
    Outbreak outbreak = mock(Outbreak.class);
    ContactReader reader = mock(ContactReader.class);
    CompartmentWriter compartmentWriter = mock(CompartmentWriter.class);
    InputFiles inputFiles = mock(InputFiles.class);
    CompletionEventListGroup eventList = mock(CompletionEventListGroup.class);

    // provide junk file for reader
    File temp = File.createTempFile("test", "file");
    when(inputFiles.contactData()).thenReturn(temp.getAbsolutePath());

    // empty map returns
    Map<Integer, Case> testData = new HashMap<>();
    List<ContactEvent> testData2 = new ArrayList<>();

    // inner behaviour
    when(reader.readEvents(any())).thenReturn(testData2);
    when(populationGenerator.generate()).thenReturn(testData);

    when(eventList.lastContactTime()).thenReturn(10);

    // setting context
    when(applicationContext.getBean(PopulationGenerator.class)).thenReturn(populationGenerator);
    when(applicationContext.getBean(ContactReader.class)).thenReturn(reader);
    when(applicationContext.getBean(CompletionEventListGroup.class)).thenReturn(eventList);
    when(applicationContext.getBean(Outbreak.class)).thenReturn(outbreak);
    when(applicationContext.getBean(CompartmentWriter.class)).thenReturn(compartmentWriter);

    runner.setApplicationContext(applicationContext);
    runner.setInputFileLocation(inputFiles);
    runner.run();

    Assert.assertThat(
        logSpy.getOutput(), containsString("Generated Population and Parsed Contact data"));
    Assert.assertThat(logSpy.getOutput(), containsString("Initialised Outbreak"));
    Assert.assertThat(logSpy.getOutput(), containsString("Writing Compartment Records"));
    Assert.assertThat(logSpy.getOutput(), containsString("Completed. Tidying up."));

    System.out.println(logSpy.getOutput());
  }

  @Test(expected = CsvException.class)
  public void writeCompartments() throws IOException {

    File temp = File.createTempFile("test", "file");
    List<CmptRecord> var = new ArrayList<>();

    CompartmentWriter compartmentWriter = mock(CompartmentWriter.class);
    doThrow(new IOException()).when(compartmentWriter).write(any(), any());
    when(applicationContext.getBean(CompartmentWriter.class)).thenReturn(compartmentWriter);
    try {
      runner.writeCompartments(var, temp);
    } catch (CsvException e) {
      System.out.println(logSpy.getOutput());
      Assert.assertThat(logSpy.getOutput(), containsString(""));
      throw e;
    }
  }
}
