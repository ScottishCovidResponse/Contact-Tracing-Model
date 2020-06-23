package uk.co.ramp.io.types;

import static uk.co.ramp.people.VirusStatus.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.immutables.value.Value;
import uk.co.ramp.people.VirusStatus;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
@JsonPropertyOrder({"time", "s", "e", "a", "p", "sym", "sev", "r", "d"})
public interface CmptRecord {

  static CmptRecord of(int time, Map<VirusStatus, Integer> counts) {
    return ImmutableCmptRecord.builder()
        .time(time)
        .s(counts.get(SUSCEPTIBLE))
        .e(counts.get(EXPOSED))
        .a(counts.get(ASYMPTOMATIC))
        .p(counts.get(PRESYMPTOMATIC))
        .sym(counts.get(SYMPTOMATIC))
        .sev(counts.get(SEVERELY_SYMPTOMATIC))
        .r(counts.get(RECOVERED))
        .d(counts.get(DEAD))
        .build();
  }

  int time();

  int s();

  int e();

  int a();

  int p();

  int sym();

  int sev();

  int r();

  int d();
}
