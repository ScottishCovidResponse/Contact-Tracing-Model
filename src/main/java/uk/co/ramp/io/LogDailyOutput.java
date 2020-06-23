package uk.co.ramp.io;

import static uk.co.ramp.people.VirusStatus.*;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.ImmutableCmptRecord;
import uk.co.ramp.people.VirusStatus;

@Service
public class LogDailyOutput {

  private static final Logger LOGGER = LogManager.getLogger(LogDailyOutput.class);
  private int previousActiveCases = 0;

  public CmptRecord log(int time, Map<VirusStatus, Integer> stats) {
    if (time == 0) {
      LOGGER.info(
          "|   Time  |    S    |    E    |    A    |    P    |   Sym   |   Sev   |    R    |    D    |   dAct  |");
    }
    int activeCases = activeCases(stats);
    int dActive = activeCases - previousActiveCases;

    CmptRecord cmptRecord =
        ImmutableCmptRecord.builder()
            .time(time)
            .s(stats.get(SUSCEPTIBLE))
            .e(stats.get(EXPOSED))
            .a(stats.get(ASYMPTOMATIC))
            .p(stats.get(PRESYMPTOMATIC))
            .sym(stats.get(SYMPTOMATIC))
            .sev(stats.get(SEVERELY_SYMPTOMATIC))
            .r(stats.get(RECOVERED))
            .d(stats.get(DEAD))
            .build();

    String s =
        String.format(
            "| %7d | %7d | %7d | %7d | %7d | %7d | %7d | %7d | %7d | %7d |",
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
    previousActiveCases = activeCases;
    return cmptRecord;
  }

  private int activeCases(Map<VirusStatus, Integer> stats) {
    return stats.get(EXPOSED)
        + stats.get(ASYMPTOMATIC)
        + stats.get(PRESYMPTOMATIC)
        + stats.get(SYMPTOMATIC)
        + stats.get(SEVERELY_SYMPTOMATIC);
  }
}
