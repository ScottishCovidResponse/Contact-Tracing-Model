package uk.co.ramp.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.co.ramp.LogSpy;
import uk.co.ramp.TestUtils;
import uk.co.ramp.people.VirusStatus;

import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static uk.co.ramp.people.VirusStatus.*;

public class LogDailyOutputTest {

    private final Random random = TestUtils.getRandom();
    @Rule
    public LogSpy logSpy = new LogSpy();
    private LogDailyOutput logger;

    @Before
    public void setup() {
        logger = new LogDailyOutput();
    }

    @Test
    public void logTestResults() {

        int time = random.nextInt(10);

        Map<VirusStatus, Integer> map = new EnumMap<>(VirusStatus.class);
        for (VirusStatus v : values()) {
            map.put(v, random.nextInt(100));
        }

        Set<VirusStatus> active = Set.of(EXPOSED, ASYMPTOMATIC, PRESYMPTOMATIC, SYMPTOMATIC, SEVERELY_SYMPTOMATIC);

        int dActive = map.entrySet().stream().filter(e -> active.contains(e.getKey())).mapToInt(Map.Entry::getValue).sum();
        logger.log(time, map);

        int[] numbers = Arrays.stream(logSpy.getOutput().replace("[INFO]", "")
                .replaceAll("(?m:\\||$)", "").trim().split("\\s+"))
                .mapToInt(Integer::parseInt).toArray();


        Assert.assertEquals(values().length + 2, numbers.length);
        Assert.assertEquals(time, numbers[0]);
        Assert.assertEquals(map.get(SUSCEPTIBLE).intValue(), numbers[1]);
        Assert.assertEquals(map.get(EXPOSED).intValue(), numbers[2]);
        Assert.assertEquals(map.get(ASYMPTOMATIC).intValue(), numbers[3]);
        Assert.assertEquals(map.get(PRESYMPTOMATIC).intValue(), numbers[4]);
        Assert.assertEquals(map.get(SYMPTOMATIC).intValue(), numbers[5]);
        Assert.assertEquals(map.get(SEVERELY_SYMPTOMATIC).intValue(), numbers[6]);
        Assert.assertEquals(map.get(RECOVERED).intValue(), numbers[7]);
        Assert.assertEquals(map.get(DEAD).intValue(), numbers[8]);
        Assert.assertEquals(dActive, numbers[9]);


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
        Assert.assertThat(logSpy.getOutput(), containsString("|   Time  |    S    |    E    |    A    |    P    |   Sym   |   Sev   |    R    |    D    |   dAct  |"));

    }


}