package uk.co.ramp.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.ramp.people.VirusStatus;
import uk.co.ramp.record.CmptRecord;
import uk.co.ramp.record.ImmutableCmptRecord;

import java.util.Map;

import static uk.co.ramp.people.VirusStatus.*;

public class LogDailyOutput {

    private static final Logger LOGGER = LogManager.getLogger(LogDailyOutput.class);

    public CmptRecord log(int time, Map<VirusStatus, Integer> stats) {
        if (time == 0) {
            LOGGER.info("|   Time  |    S    |    E1   |    E2   |   Ia    |    Is   |    R    |    D    |");
        }

        CmptRecord cmptRecord = ImmutableCmptRecord.builder().time(time).
                s(stats.get(SUSCEPTIBLE)).
                e1(stats.get(EXPOSED)).
                e2(stats.get(EXPOSED_2)).
                ia(stats.get(INFECTED)).
                is(stats.get(INFECTED_SYMP)).
                r(stats.get(RECOVERED)).
                d(stats.get(DEAD)).build();

        String s = String.format("| %7d | %7d | %7d | %7d | %7d | %7d | %7d | %7d |",
                cmptRecord.time(),
                cmptRecord.s(),
                cmptRecord.e1(),
                cmptRecord.e2(),
                cmptRecord.ia(),
                cmptRecord.is(),
                cmptRecord.r(),
                cmptRecord.d());

        LOGGER.info(s);
        return cmptRecord;
    }


}
