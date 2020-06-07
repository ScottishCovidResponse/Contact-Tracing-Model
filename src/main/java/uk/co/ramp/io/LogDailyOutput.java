package uk.co.ramp.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.ImmutableCmptRecord;
import uk.co.ramp.people.VirusStatus;

import java.util.Map;

import static uk.co.ramp.people.VirusStatus.*;

@Service
public class LogDailyOutput {

    private static final Logger LOGGER = LogManager.getLogger(LogDailyOutput.class);

    public CmptRecord log(int time, Map<VirusStatus, Integer> stats, int dActive) {
        if (time == 0) {
            LOGGER.info("|   Time  |    S    |    E    |    A    |    P    |   Sym   |   Sev   |    R    |    D    |   dAct  |");
        }

        CmptRecord cmptRecord = ImmutableCmptRecord.builder().time(time).
                s(stats.get(SUSCEPTIBLE)).
                e(stats.get(EXPOSED)).
                a(stats.get(ASYMPTOMATIC)).
                p(stats.get(PRESYMPTOMATIC)).
                sym(stats.get(SYMPTOMATIC)).
                sev(stats.get(SEVERELY_SYMPTOMATIC)).
                r(stats.get(RECOVERED)).
                d(stats.get(DEAD)).build();

        String s = String.format("| %7d | %7d | %7d | %7d | %7d | %7d | %7d | %7d | %7d | %7d |",
                cmptRecord.time(),
                cmptRecord.s(),
                cmptRecord.e(),
                cmptRecord.a(),
                cmptRecord.p(),
                cmptRecord.sym(),
                cmptRecord.sev(),
                cmptRecord.r(),
                cmptRecord.d(),
                dActive);

        LOGGER.info(s);
        return cmptRecord;
    }


}
