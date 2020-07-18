package uk.co.ramp.io;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.ramp.people.VirusStatus.*;

import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.co.ramp.LogSpy;
import uk.co.ramp.TestUtils;
import uk.co.ramp.io.types.StandardProperties;
import uk.co.ramp.people.VirusStatus;

public class LogDailyOutputTest {

  private final Random random = TestUtils.getRandom();
  @Rule public LogSpy logSpy = new LogSpy();
  private LogDailyOutput logger;

  @Before
  public void setup() {
    StandardProperties properties = mock(StandardProperties.class);
    when(properties.timeStepsPerDay()).thenReturn(1);
    logger = new LogDailyOutput(properties);
  }

  @Test
  public void logTestResults() {

    int time = random.nextInt(10);

    Map<VirusStatus, Integer> map = new EnumMap<>(VirusStatus.class);
    for (VirusStatus v : values()) {
      map.put(v, random.nextInt(100));
    }

    Set<VirusStatus> active =
        Set.of(EXPOSED, ASYMPTOMATIC, PRESYMPTOMATIC, SYMPTOMATIC, SEVERELY_SYMPTOMATIC);

    int dActive =
        map.entrySet().stream()
            .filter(e -> active.contains(e.getKey()))
            .mapToInt(Map.Entry::getValue)
            .sum();
    logger.log(time, map);

    String log = logSpy.getOutput();

    int secondPipe = log.indexOf("|", log.indexOf("|") + 1);

    double timeExtract =
        Double.parseDouble(log.substring(0, secondPipe).substring(log.indexOf("|") + 1));
    String logOut = log.substring(secondPipe);

    int[] numbers =
        Arrays.stream(
                logOut.replace("[INFO]", "").replaceAll("(?m:\\||$)", "").trim().split("\\s+"))
            .mapToInt(Integer::parseInt)
            .toArray();
    int i = 0;

    Assert.assertEquals(values().length + 1, numbers.length);
    Assert.assertEquals(time, timeExtract, 1e-6);
    Assert.assertEquals(map.get(SUSCEPTIBLE).intValue(), numbers[i++]);
    Assert.assertEquals(map.get(EXPOSED).intValue(), numbers[i++]);
    Assert.assertEquals(map.get(ASYMPTOMATIC).intValue(), numbers[i++]);
    Assert.assertEquals(map.get(PRESYMPTOMATIC).intValue(), numbers[i++]);
    Assert.assertEquals(map.get(SYMPTOMATIC).intValue(), numbers[i++]);
    Assert.assertEquals(map.get(SEVERELY_SYMPTOMATIC).intValue(), numbers[i++]);
    Assert.assertEquals(map.get(RECOVERED).intValue(), numbers[i++]);
    Assert.assertEquals(map.get(DEAD).intValue(), numbers[i++]);
    Assert.assertEquals(dActive, numbers[i++]);
  }

  @Test
  public void logTestResultsHeader() {

    int time = 0;
    Map<VirusStatus, Integer> map = new EnumMap<>(VirusStatus.class);

    for (VirusStatus v : values()) {
      map.put(v, random.nextInt(100));
    }
    //        int previousActiveCases = 0;
    logger.log(time, map);
    //        logger.log(time, map, previousActiveCases);
    Assert.assertThat(
        logSpy.getOutput(),
        containsString(
            "|   Time  |    S    |    E    |    A    |    P    |   Sym   |   Sev   |    R    |    D    |   dAct  |"));
  }
}
