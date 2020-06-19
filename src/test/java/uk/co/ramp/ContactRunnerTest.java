package uk.co.ramp;

import org.junit.*;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import uk.co.ramp.event.EventList;
import uk.co.ramp.event.FormattedEventFactory;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.io.CompartmentWriter;
import uk.co.ramp.io.ContactReader;
import uk.co.ramp.io.csv.CsvException;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.InputFiles;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.PopulationGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class ContactRunnerTest {

    @Rule
    public LogSpy logSpy = new LogSpy();
    private ContactRunner runner;
    private ApplicationContext applicationContext;

    @Before
    public void setUp() {
        applicationContext = Mockito.mock(ApplicationContext.class);
        runner = new ContactRunner();
        runner.setApplicationContext(applicationContext);
    }

    @After
    public void tearDown() {
        runner = null;
        applicationContext = null;
    }

    @Test
    public void run() throws IOException {

        // Stub classes
        PopulationGenerator populationGenerator = Mockito.mock(PopulationGenerator.class);
        StandardProperties runProperties = Mockito.mock(StandardProperties.class);
        Outbreak outbreak = Mockito.mock(Outbreak.class);
        ContactReader reader = Mockito.mock(ContactReader.class);
        CompartmentWriter compartmentWriter = Mockito.mock(CompartmentWriter.class);
        InputFiles inputFiles = Mockito.mock(InputFiles.class);

        // provide junk file for reader
        File temp = File.createTempFile("test", "file");
        when(inputFiles.contactData()).thenReturn(temp.getAbsolutePath());

        // empty map returns
        Map<Integer, Case> testData = new HashMap<>();
        Map<Integer, List<ContactEvent>> testData2 = new HashMap<>();


        // inner behaviour
        when(reader.readEvents(any())).thenReturn(testData2);
        when(populationGenerator.generate()).thenReturn(testData);

        // setting context
        when(applicationContext.getBean(PopulationGenerator.class)).thenReturn(populationGenerator);
        when(applicationContext.getBean(ContactReader.class)).thenReturn(reader);
        when(applicationContext.getBean(EventList.class)).thenReturn(new EventList(new FormattedEventFactory()));
        when(applicationContext.getBean(Outbreak.class)).thenReturn(outbreak);
        when(applicationContext.getBean(CompartmentWriter.class)).thenReturn(compartmentWriter);

        runner.setApplicationContext(applicationContext);
        runner.setInputFileLocation(inputFiles);
        runner.run();


        Assert.assertThat(logSpy.getOutput(), containsString("Generated Population and Parsed Contact data"));
        Assert.assertThat(logSpy.getOutput(), containsString("Initialised Outbreak"));
        Assert.assertThat(logSpy.getOutput(), containsString("Writing Compartment Records"));
        Assert.assertThat(logSpy.getOutput(), containsString("Completed. Tidying up."));

        System.out.println(logSpy.getOutput());


    }

    @Test(expected = CsvException.class)
    public void writeCompartments() throws IOException {

        File temp = File.createTempFile("test", "file");
        List<CmptRecord> var = new ArrayList<>();

        CompartmentWriter compartmentWriter = Mockito.mock(CompartmentWriter.class);
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