package uk.co.ramp.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.VirusStatus.*;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.ramp.AppConfig;
import uk.co.ramp.Population;
import uk.co.ramp.TestConfig;
import uk.co.ramp.TestUtils;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.event.types.*;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.people.Case;
import uk.co.ramp.people.Human;
import uk.co.ramp.policy.alert.AlertChecker;
import uk.co.ramp.policy.alert.TracingPolicyContext;

@RunWith(SpringRunner.class)
@DirtiesContext
@Import({TestUtils.class, AppConfig.class, TestConfig.class, TracingPolicyContext.class})
public class VirusEventProcessorTest {
  private VirusEventProcessor eventProcessor;
  private DiseaseProperties diseaseProperties;

  private final AlertEvent alertEvent =
      ImmutableAlertEvent.builder()
          .id(0)
          .oldStatus(NONE)
          .nextStatus(REQUESTED_TEST)
          .time(2)
          .build();

  @Autowired DistributionSampler distributionSampler;

  @Before
  public void setUp() throws FileNotFoundException {
    diseaseProperties = TestUtils.diseaseProperties();

    Human human = mock(Human.class);
    when(human.health()).thenReturn(-1d);
    Case aCase = new Case(human);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, aCase);

    AlertChecker alertChecker = mock(AlertChecker.class);
    when(alertChecker.checkForAlert(eq(0), eq(SYMPTOMATIC), eq(1)))
        .thenReturn(Stream.of(alertEvent));

    this.eventProcessor =
        new VirusEventProcessor(
            new Population(population), diseaseProperties, distributionSampler, alertChecker);
  }

  @Test
  public void checkForAlert() {

    VirusEvent virusEvent =
        ImmutableVirusEvent.builder()
            .id(0)
            .oldStatus(EXPOSED)
            .nextStatus(PRESYMPTOMATIC)
            .time(1)
            .build();
    assertThat(eventProcessor.checkForAlert(virusEvent)).isEmpty();

    virusEvent =
        ImmutableVirusEvent.builder()
            .id(0)
            .oldStatus(PRESYMPTOMATIC)
            .nextStatus(SYMPTOMATIC)
            .time(1)
            .build();
    assertThat(eventProcessor.checkForAlert(virusEvent)).containsExactly(alertEvent);
  }

  @Test
  public void runVirusEvents() {

    int infector = 0;

    Case mock0 = mock(Case.class);
    when(mock0.virusStatus()).thenReturn(EXPOSED);
    when(mock0.isInfectious()).thenReturn(true);
    when(mock0.id()).thenReturn(infector);

    Map<Integer, Case> population = new HashMap<>();
    population.put(0, mock0);

    ReflectionTestUtils.setField(eventProcessor, "population", new Population(population));

    VirusEvent event =
        ImmutableVirusEvent.builder()
            .time(0)
            .id(0)
            .oldStatus(EXPOSED)
            .nextStatus(ASYMPTOMATIC)
            .build();

    ProcessedEventResult processedEventResult = eventProcessor.processEvent(event);

    Assert.assertEquals(1, processedEventResult.newVirusEvents().size());
    Assert.assertEquals(0, processedEventResult.newAlertEvents().size());
    Assert.assertEquals(0, processedEventResult.newContactEvents().size());
    Assert.assertEquals(0, processedEventResult.newInfectionEvents().size());
    Assert.assertEquals(1, processedEventResult.newCompletedVirusEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedInfectionEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedAlertEvents().size());
    Assert.assertEquals(0, processedEventResult.newCompletedContactEvents().size());

    VirusEvent evnt = processedEventResult.newVirusEvents().get(0);

    Assert.assertEquals(diseaseProperties.timeLatent().mean(), evnt.time());
    Assert.assertEquals(0, evnt.id());
    Assert.assertEquals(ASYMPTOMATIC, evnt.oldStatus());
    Assert.assertTrue(ASYMPTOMATIC.getValidTransitions().contains(evnt.nextStatus()));
  }
}
